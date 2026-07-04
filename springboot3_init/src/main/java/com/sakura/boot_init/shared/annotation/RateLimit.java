package com.sakura.boot_init.shared.annotation;

import com.sakura.boot_init.shared.enums.RateLimitDimension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Redis 分布式限流注解。
 *
 * @author Sakura
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Redis key 前缀，建议使用模块:动作，例如 upload:image。
     */
    String prefix();

    /**
     * SpringEL 表达式，用于生成业务 key 后缀，例如 #biz 或 #request.userId。
     */
    String key() default "";

    /**
     * 固定窗口内允许的最大请求次数。
     */
    int limit();

    /**
     * 固定窗口长度，单位秒。
     */
    long windowSeconds();

    /**
     * 限流主体维度，默认已登录按用户、未登录按 IP。
     */
    RateLimitDimension dimension() default RateLimitDimension.USER_OR_IP;

    /**
     * 超过限流阈值时返回给前端的提示文案。
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * Redis 判断失败时是否拒绝请求，默认 false 表示降级放行。
     */
    boolean failClosed() default false;
}
