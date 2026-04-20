package com.sakura.boot_init.observability;

import com.sakura.boot_init.observability.config.ObservabilityProperties;
import com.sakura.boot_init.observability.enums.ObservabilityStatusLevelEnum;
import com.sakura.boot_init.observability.service.impl.LoginFailureWindowService;
import com.sakura.boot_init.observability.service.impl.JvmMemoryMetricBuilder;
import com.sakura.boot_init.observability.support.ObservabilitySanitizer;
import com.sakura.boot_init.shared.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.concurrent.TimeUnit;
import java.lang.management.MemoryUsage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * 可观测性模块基础规则测试，约束脱敏、阈值和短窗口统计行为。
 */
class ObservabilitySupportTest {

    /**
     * 请求摘要和异常摘要必须先脱敏再展示或落库。
     */
    @Test
    void shouldSanitizeSensitiveContent() {
        ObservabilitySanitizer sanitizer = new ObservabilitySanitizer(80);

        String result = sanitizer.sanitize("{\"password\":\"123456\",\"token\":\"abc\"}&authorization=Bearer xyz&normal=ok");

        assertTrue(result.contains("\"password\":\"***\""));
        assertTrue(result.contains("\"token\":\"***\""));
        assertTrue(result.contains("authorization=***"));
        assertTrue(result.contains("normal=ok"));
        assertTrue(result.length() <= 80);
    }

    /**
     * 百分比状态等级必须遵循健康、警告、严重三个阈值。
     */
    @Test
    void shouldResolveStatusLevelByThreshold() {
        assertEquals(ObservabilityStatusLevelEnum.UP, ObservabilityStatusLevelEnum.fromUsage(50, 80, 90));
        assertEquals(ObservabilityStatusLevelEnum.DEGRADED, ObservabilityStatusLevelEnum.fromUsage(85, 80, 90));
        assertEquals(ObservabilityStatusLevelEnum.DOWN, ObservabilityStatusLevelEnum.fromUsage(95, 80, 90));
    }

    /**
     * 非堆内存没有可靠 max 时，不能用 committed 作为告警容量，否则容易把正常增长误判为异常。
     */
    @Test
    void shouldNotMarkNonHeapDownWhenMaxIsUnavailable() {
        JvmMemoryMetricBuilder builder = new JvmMemoryMetricBuilder(80, 90);
        MemoryUsage nonHeapUsage = new MemoryUsage(0, 122L * 1024 * 1024, 124L * 1024 * 1024, -1);

        assertEquals(ObservabilityStatusLevelEnum.UP.getValue(),
                builder.buildNonHeapMemoryMetric("非堆内存", nonHeapUsage).getStatus());
    }

    /**
     * 即使 JVM 返回了非堆 max，也不能把非堆内存接近上限直接判定为系统异常。
     */
    @Test
    void shouldNotMarkNonHeapDownWhenMaxIsPositive() {
        JvmMemoryMetricBuilder builder = new JvmMemoryMetricBuilder(80, 90);
        MemoryUsage nonHeapUsage = new MemoryUsage(0, 122L * 1024 * 1024,
                124L * 1024 * 1024, 124L * 1024 * 1024);

        assertEquals(ObservabilityStatusLevelEnum.UP.getValue(),
                builder.buildNonHeapMemoryMetric("非堆内存", nonHeapUsage).getStatus());
    }

    /**
     * 登录失败短窗口统计应在首次递增时设置 TTL，并能返回阈值命中状态。
     */
    @Test
    void shouldCountLoginFailuresWithWindowTtl() {
        ObservabilityProperties properties = new ObservabilityProperties();
        properties.setLoginFailureWindowSeconds(600);
        properties.setLoginFailureIpThreshold(3);
        LoginFailureWindowService service = new LoginFailureWindowService(properties);

        try (MockedStatic<RedisUtil> redisUtil = mockStatic(RedisUtil.class)) {
            redisUtil.when(() -> RedisUtil.increment("observability:login-failure:ip:127.0.0.1", 1)).thenReturn(3L);

            long count = service.incrementIpFailure("127.0.0.1");

            assertEquals(3L, count);
            assertTrue(service.reachesIpThreshold(count));
            redisUtil.verify(() -> RedisUtil.expire("observability:login-failure:ip:127.0.0.1", 600, TimeUnit.SECONDS));
        }
    }
}
