package com.sakura.boot_init.infrastructure.ratelimit;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Redis 固定窗口限流执行器。
 *
 * @author Sakura
 */
@Component
public class RedisRateLimiter {

    /**
     * Redis 原子计数脚本，避免 INCR 成功但 EXPIRE 失败造成永久 key。
     * 返回值依次为：当前计数、剩余 TTL、是否为本窗口第一次超限。
     */
    private static final String RATE_LIMIT_SCRIPT = """
            local current = redis.call('incr', KEYS[1])
            if current == 1 then
                redis.call('expire', KEYS[1], tonumber(ARGV[1]))
            end
            local ttl = redis.call('ttl', KEYS[1])
            local firstLimited = 0
            if current == tonumber(ARGV[2]) + 1 then
                firstLimited = 1
            end
            return { current, ttl, firstLimited }
            """;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<List> redisScript;

    /**
     * 初始化 Lua 脚本对象，避免每次请求重复创建。
     */
    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(RATE_LIMIT_SCRIPT);
        redisScript.setResultType(List.class);
    }

    /**
     * 对指定 key 做一次固定窗口计数。
     */
    public RateLimitResult acquire(String key, int limit, long windowSeconds) {
        List<?> result = stringRedisTemplate.execute(redisScript, Collections.singletonList(key),
                String.valueOf(windowSeconds), String.valueOf(limit));
        if (result == null || result.size() < 3) {
            throw new IllegalStateException("Redis rate limit script returned empty result");
        }
        long current = Long.parseLong(String.valueOf(result.get(0)));
        long ttl = Long.parseLong(String.valueOf(result.get(1)));
        boolean firstLimited = Long.parseLong(String.valueOf(result.get(2))) == 1L;
        return new RateLimitResult(current <= limit, current, limit, Math.max(ttl, 0), firstLimited);
    }
}
