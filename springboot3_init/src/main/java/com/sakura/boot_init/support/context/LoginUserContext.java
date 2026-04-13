package com.sakura.boot_init.support.context;

import com.sakura.boot_init.user.model.entity.User;

/**
 * 当前请求登录用户上下文。
 */
public final class LoginUserContext {

    /**
     * 当前线程绑定的登录用户。
     */
    private static final ThreadLocal<User> LOGIN_USER_HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    /**
     * 保存当前请求的登录用户。
     *
     * @param user 登录用户
     */
    public static void setLoginUser(User user) {
        LOGIN_USER_HOLDER.set(user);
    }

    /**
     * 获取当前请求的登录用户。
     *
     * @return 登录用户
     */
    public static User getLoginUser() {
        return LOGIN_USER_HOLDER.get();
    }

    /**
     * 清理当前线程中的登录用户，避免线程复用导致数据串扰。
     */
    public static void clear() {
        LOGIN_USER_HOLDER.remove();
    }
}
