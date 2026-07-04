package com.sakura.boot_init.shared.event;

import java.util.Date;

/**
 * 请求被限流拒绝事件，由基础设施发布，审计等模块按需监听。
 *
 * @param userId 用户 id
 * @param accountIdentifier 账号标识
 * @param ipAddress IP 地址
 * @param clientInfo 客户端信息
 * @param requestPath 请求路径
 * @param httpMethod HTTP 方法
 * @param traceId 追踪 ID
 * @param rateLimitKey Redis 限流 key
 * @param rulePrefix 限流规则前缀
 * @param dimension 限流维度
 * @param current 当前窗口计数
 * @param limit 当前窗口阈值
 * @param ttlSeconds 当前窗口剩余秒数
 * @param occurredAt 发生时间
 */
public record RateLimitRejectedEvent(
        Long userId,
        String accountIdentifier,
        String ipAddress,
        String clientInfo,
        String requestPath,
        String httpMethod,
        String traceId,
        String rateLimitKey,
        String rulePrefix,
        String dimension,
        long current,
        int limit,
        long ttlSeconds,
        Date occurredAt
) {
}
