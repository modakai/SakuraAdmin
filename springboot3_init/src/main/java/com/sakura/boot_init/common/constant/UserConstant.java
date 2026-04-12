package com.sakura.boot_init.common.constant;

/**
 * 用户常量
 *
 * 作者：Sakura
 */
public interface UserConstant {

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    /**
     * 用户密码加密盐值。
     */
    String PASSWORD_SALT = "sakura";

    // endregion
}

