package com.sakura.boot_init.infra.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Token 生成与 Redis 登录态管理。
 */
@Component
public class TokenManager {

    /**
     * token 到用户 id 的 Redis key 前缀。
     */
    public static final String TOKEN_KEY_PREFIX = "login:token:";

    /**
     * 用户 id 到 token 的 Redis key 前缀。
     */
    public static final String USER_KEY_PREFIX = "login:user:";

    /**
     * 默认 token 有效期，单位天。
     */
    public static final long TOKEN_EXPIRE_DAYS = 30L;

    /**
     * token 请求头名称。
     */
    public static final String TOKEN_HEADER = "token";

    /**
     * Authorization 请求头名称。
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * token 可选字符集，包含数字、字母和特殊符号。
     */
    private static final char[] DIGIT_CHARS = "0123456789".toCharArray();

    /**
     * token 字母字符集。
     */
    private static final char[] LETTER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * token 特殊符号字符集。
     */
    private static final char[] SYMBOL_CHARS = "!@#$%^&*()_+-=[]{};:,.?/".toCharArray();

    /**
     * token 完整可选字符集。
     */
    private static final char[] TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+-=[]{};:,.?/".toCharArray();

    /**
     * 安全随机数生成器。
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Redis 字符串操作模板。
     */
    private final StringRedisTemplate redisTemplate;

    public TokenManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成 32 位随机 token。
     *
     * @return token 字符串
     */
    public String generateToken() {
        char[] token = new char[32];
        // 先保证 token 至少包含数字、字母和特殊符号三类字符。
        token[0] = randomChar(DIGIT_CHARS);
        token[1] = randomChar(LETTER_CHARS);
        token[2] = randomChar(SYMBOL_CHARS);
        for (int i = 3; i < token.length; i++) {
            token[i] = TOKEN_CHARS[RANDOM.nextInt(TOKEN_CHARS.length)];
        }
        shuffle(token);
        return new String(token);
    }

    /**
     * 保存 token 与用户 id 的双向映射。
     *
     * @param userId 用户 id
     * @param token  token
     */
    public void storeToken(Long userId, String token) {
        String userKey = buildUserKey(userId);
        String oldToken = redisTemplate.opsForValue().get(userKey);
        if (StringUtils.isNotBlank(oldToken)) {
            redisTemplate.delete(buildTokenKey(oldToken));
        }
        redisTemplate.opsForValue().set(buildTokenKey(token), String.valueOf(userId), TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(userKey, token, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 根据 token 获取用户 id。
     *
     * @param token token
     * @return 用户 id，不存在则返回 null
     */
    public Long getUserId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String userId = redisTemplate.opsForValue().get(buildTokenKey(token));
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return Long.valueOf(userId);
    }

    /**
     * 删除 token 登录态。
     *
     * @param token token
     */
    public void removeToken(String token) {
        Long userId = getUserId(token);
        if (userId != null) {
            redisTemplate.delete(buildUserKey(userId));
        }
        if (StringUtils.isNotBlank(token)) {
            redisTemplate.delete(buildTokenKey(token));
        }
    }

    /**
     * 从请求中解析 token，优先读取 Authorization: Bearer xxx。
     *
     * @param request HTTP 请求
     * @return token，不存在则返回 null
     */
    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            return StringUtils.substringAfter(authorization, "Bearer ");
        }
        return request.getHeader(TOKEN_HEADER);
    }

    /**
     * 构造 token 映射 key。
     *
     * @param token token
     * @return Redis key
     */
    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    /**
     * 构造用户映射 key。
     *
     * @param userId 用户 id
     * @return Redis key
     */
    private String buildUserKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    /**
     * 从指定字符集中随机获取一个字符。
     *
     * @param chars 字符集
     * @return 随机字符
     */
    private char randomChar(char[] chars) {
        return chars[RANDOM.nextInt(chars.length)];
    }

    /**
     * 打乱 token 字符顺序，避免固定位置暴露字符类型。
     *
     * @param token token 字符数组
     */
    private void shuffle(char[] token) {
        for (int i = token.length - 1; i > 0; i--) {
            int index = RANDOM.nextInt(i + 1);
            char temp = token[index];
            token[index] = token[i];
            token[i] = temp;
        }
    }
}
