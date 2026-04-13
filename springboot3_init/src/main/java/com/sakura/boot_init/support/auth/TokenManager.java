package com.sakura.boot_init.support.auth;

import com.sakura.boot_init.support.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Token 生成与 Redis 登录态管理器。
 *
 * 作者：Sakura
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
     * token 默认有效期，单位天。
     */
    public static final long TOKEN_EXPIRE_DAYS = 30L;

    /**
     * 自定义 token 请求头。
     */
    public static final String TOKEN_HEADER = "token";

    /**
     * 标准认证请求头。
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * 数字字符集。
     */
    private static final char[] DIGIT_CHARS = "0123456789".toCharArray();

    /**
     * 字母字符集。
     */
    private static final char[] LETTER_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * 特殊字符集。
     */
    private static final char[] SYMBOL_CHARS = "!@#$%^&*()_+[]{};,.?".toCharArray();

    /**
     * 完整可选字符集。
     */
    private static final char[] TOKEN_CHARS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+[]{};:,.?/"
                    .toCharArray();

    /**
     * 安全随机数生成器。
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成 32 位随机 token，并保证至少包含数字、字母和特殊字符。
     *
     * @return token 字符串
     */
    public String generateToken() {
        char[] tokenChars = new char[32];
        tokenChars[0] = randomChar(DIGIT_CHARS);
        tokenChars[1] = randomChar(LETTER_CHARS);
        tokenChars[2] = randomChar(SYMBOL_CHARS);
        for (int i = 3; i < tokenChars.length; i++) {
            tokenChars[i] = randomChar(TOKEN_CHARS);
        }
        shuffle(tokenChars);
        return new String(tokenChars);
    }

    /**
     * 保存 token 和用户 id 的双向映射。
     *
     * @param userId 用户 id
     * @param token token
     */
    public void storeToken(Long userId, String token) {
        String userKey = buildUserKey(userId);
        String oldToken = RedisUtil.getCacheObject(userKey);
        if (StringUtils.isNotBlank(oldToken)) {
            RedisUtil.deleteObject(buildTokenKey(oldToken));
        }
        RedisUtil.setCacheObject(buildTokenKey(token), String.valueOf(userId), (int) TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
        RedisUtil.setCacheObject(userKey, token, (int) TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 根据 token 获取用户 id。
     *
     * @param token token
     * @return 用户 id，不存在时返回 null
     */
    public Long getUserId(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        String userId = RedisUtil.getCacheObject(buildTokenKey(token));
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return Long.valueOf(userId);
    }

    /**
     * 删除登录态。
     *
     * @param token token
     */
    public void removeToken(String token) {
        Long userId = getUserId(token);
        if (userId != null) {
            RedisUtil.deleteObject(buildUserKey(userId));
        }
        if (StringUtils.isNotBlank(token)) {
            RedisUtil.deleteObject(buildTokenKey(token));
        }
    }

    /**
     * 从请求中解析 token。
     *
     * @param request HTTP 请求
     * @return token，不存在时返回 null
     */
    public String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.startsWithIgnoreCase(authorization, "Bearer ")) {
            return StringUtils.substringAfter(authorization, "Bearer ");
        }
        return request.getHeader(TOKEN_HEADER);
    }

    /**
     * 构造 token key。
     *
     * @param token token
     * @return Redis key
     */
    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }

    /**
     * 构造用户 key。
     *
     * @param userId 用户 id
     * @return Redis key
     */
    private String buildUserKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    /**
     * 从指定字符集中随机取一个字符。
     *
     * @param chars 字符集
     * @return 随机字符
     */
    private char randomChar(char[] chars) {
        return chars[RANDOM.nextInt(chars.length)];
    }

    /**
     * 打乱字符顺序，避免固定位置暴露字符类别。
     *
     * @param tokenChars token 字符数组
     */
    private void shuffle(char[] tokenChars) {
        for (int i = tokenChars.length - 1; i > 0; i--) {
            int index = RANDOM.nextInt(i + 1);
            char temp = tokenChars[index];
            tokenChars[index] = tokenChars[i];
            tokenChars[i] = temp;
        }
    }
}
