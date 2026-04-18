package com.sakura.boot_init.audit.enums;

/**
 * 审计操作类型枚举。
 *
 * @author Sakura
 */
public enum AuditOperationTypeEnum {

    /**
     * 创建。
     */
    CREATE("create"),

    /**
     * 更新。
     */
    UPDATE("update"),

    /**
     * 删除。
     */
    DELETE("delete"),

    /**
     * 查询。
     */
    QUERY("query"),

    /**
     * 导出。
     */
    EXPORT("export"),

    /**
     * 登录。
     */
    LOGIN("login"),

    /**
     * 其他操作。
     */
    OTHER("other");

    /**
     * 存储值。
     */
    private final String value;

    AuditOperationTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
