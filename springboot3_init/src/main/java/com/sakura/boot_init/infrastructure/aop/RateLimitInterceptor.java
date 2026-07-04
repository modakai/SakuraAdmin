package com.sakura.boot_init.infrastructure.aop;


import com.sakura.boot_init.infrastructure.ratelimit.RateLimitKeyResolver;
import com.sakura.boot_init.infrastructure.ratelimit.RateLimitResult;
import com.sakura.boot_init.infrastructure.ratelimit.RedisRateLimiter;
import com.sakura.boot_init.shared.annotation.RateLimit;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.event.RateLimitRejectedEvent;
import com.sakura.boot_init.shared.exception.BusinessException;
import com.sakura.boot_init.shared.util.NetUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.UUID;

/**
 * 自定义限流注解切面。
 *
 * @author Sakura
 */
@Aspect
@Component
@Slf4j
public class RateLimitInterceptor {

    @Resource
    private RateLimitKeyResolver keyResolver;

    @Resource
    private RedisRateLimiter redisRateLimiter;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 拦截带 @RateLimit 的方法，超限时阻断业务方法执行。
     */
    @Around("@annotation(rateLimit)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        validate(rateLimit);
        String key = keyResolver.resolve(joinPoint, rateLimit);
        try {
            RateLimitResult result = redisRateLimiter.acquire(key, rateLimit.limit(), rateLimit.windowSeconds());
            if (!result.allowed()) {
                log.warn("request rate limited, key={}, current={}, limit={}, ttl={}",
                        key, result.current(), result.limit(), result.ttlSeconds());
                if (result.firstLimited()) {
                    publishRateLimitRejectedEvent(key, rateLimit, result);
                }
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            if (rateLimit.failClosed()) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, rateLimit.message());
            }
            // Redis 异常默认降级放行，避免保护层变成业务单点故障。
            log.error("rate limit check failed, request will be allowed, key={}", key, e);
        }
        return joinPoint.proceed();
    }

    /**
     * 校验注解参数，避免错误配置静默失效。
     */
    private void validate(RateLimit rateLimit) {
        if (rateLimit.limit() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "@RateLimit limit must be greater than 0");
        }
        if (rateLimit.windowSeconds() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "@RateLimit windowSeconds must be greater than 0");
        }
    }

    /**
     * 发布限流拒绝事件，由审计模块异步监听并写入业务审计表。
     */
    private void publishRateLimitRejectedEvent(String key, RateLimit rateLimit, RateLimitResult result) {
        HttpServletRequest request = currentRequest();
        LoginUserInfo loginUser = LoginUserContext.getLoginUser();
        eventPublisher.publishEvent(new RateLimitRejectedEvent(
                loginUser == null ? null : loginUser.userId(),
                loginUser == null ? null : loginUser.userAccount(),
                request == null ? "unknown" : NetUtils.getIpAddress(request),
                request == null ? null : request.getHeader("User-Agent"),
                request == null ? null : request.getRequestURI(),
                request == null ? null : request.getMethod(),
                request == null ? UUID.randomUUID().toString() : getTraceId(request),
                key,
                rateLimit.prefix(),
                rateLimit.dimension().name(),
                result.current(),
                result.limit(),
                result.ttlSeconds(),
                new Date()
        ));
    }

    /**
     * 获取当前 HTTP 请求，非 Web 调用场景下返回 null。
     */
    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }
        return servletRequestAttributes.getRequest();
    }

    /**
     * 解析追踪 ID，优先沿用前端或网关注入的请求头。
     */
    private String getTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        return StringUtils.defaultIfBlank(traceId, UUID.randomUUID().toString());
    }
}
