package com.sakura.boot_init.infrastructure.ratelimit;

import com.sakura.boot_init.shared.annotation.RateLimit;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 限流 key 解析测试，验证 SpringEL 参数名能进入 Redis key。
 */
class RateLimitKeyResolverTest {

    private final RateLimitKeyResolver resolver = new RateLimitKeyResolver();

    @AfterEach
    void tearDown() {
        LoginUserContext.clear();
    }

    @Test
    void shouldResolveSpringElParameterNameIntoKey() throws NoSuchMethodException {
        LoginUserContext.setLoginUser(new LoginUserInfo(10001L, "admin", "管理员", "admin"));
        Method method = TestTarget.class.getDeclaredMethod("upload", String.class);
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        ProceedingJoinPoint joinPoint = mockJoinPoint(method, new Object[]{"photo_wall"});

        String key = resolver.resolve(joinPoint, rateLimit);

        assertEquals("rate_limit:upload:image:user:10001:photo_wall", key);
    }

    /**
     * 构造只包含 key 解析所需信息的切点对象。
     */
    private ProceedingJoinPoint mockJoinPoint(Method method, Object[] args) {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        TestTarget target = new TestTarget();
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    /**
     * 测试用目标方法，模拟 Controller 上的限流注解。
     */
    private static class TestTarget {

        @RateLimit(prefix = "upload:image", key = "#biz", limit = 20, windowSeconds = 60)
        void upload(String biz) {
            // 测试只关心方法签名和注解，不需要执行业务逻辑。
        }
    }
}
