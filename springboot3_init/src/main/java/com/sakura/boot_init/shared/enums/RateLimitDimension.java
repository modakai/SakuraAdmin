package com.sakura.boot_init.shared.enums;


/**
 * 限流维度枚举，用来约束 Redis key 中的主体部分。
 *
 * @author Sakura
 */
public enum RateLimitDimension {

    /**
     * 仅按客户端 IP 限流，适合登录、公开接口等未登录场景。
     */
    IP,

    /**
     * 仅按登录用户限流，未登录时应拒绝，适合后台管理接口。
     */
    USER,

    /**
     * 已登录按用户限流，未登录按 IP 兜底，适合大多数通用接口。
     */
    USER_OR_IP,

    /**
     * 完全依赖 SpringEL 表达式生成业务 key，适合非常明确的业务场景。
     */
    CUSTOM
}
