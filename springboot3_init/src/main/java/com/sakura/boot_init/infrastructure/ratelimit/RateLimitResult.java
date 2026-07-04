package com.sakura.boot_init.infrastructure.ratelimit;

/**
 * 单次限流判断结果。
 *
 * @author Sakura
 */
public record RateLimitResult(
        boolean allowed,
        long current,
        int limit,
        long ttlSeconds,
        boolean firstLimited
) {
}
