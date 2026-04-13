package com.sakura.boot_init.support.constant;

/**
 * 用户常量
 *
 * @author Sakura
 */
public interface UserConstant {

    // region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 封禁角色
     */
    String BAN_ROLE = "ban";

    /**
     * 用户密码加密盐值
     */
    String PASSWORD_SALT = "sakura";

    // endregion
}
