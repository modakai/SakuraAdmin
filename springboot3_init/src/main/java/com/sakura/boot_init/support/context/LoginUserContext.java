package com.sakura.boot_init.support.context;

import com.sakura.boot_init.user.model.entity.User;

/**
 * 褰撳墠璇锋眰鐧诲綍鐢ㄦ埛涓婁笅鏂囥€? */
public final class LoginUserContext {

    /**
     * 褰撳墠绾跨▼缁戝畾鐨勭櫥褰曠敤鎴枫€?     */
    private static final ThreadLocal<User> LOGIN_USER_HOLDER = new ThreadLocal<>();

    private LoginUserContext() {
    }

    /**
     * 淇濆瓨褰撳墠璇锋眰鐨勭櫥褰曠敤鎴枫€?     *
     * @param user 鐧诲綍鐢ㄦ埛
     */
    public static void setLoginUser(User user) {
        LOGIN_USER_HOLDER.set(user);
    }

    /**
     * 鑾峰彇褰撳墠璇锋眰鐨勭櫥褰曠敤鎴枫€?     *
     * @return 鐧诲綍鐢ㄦ埛
     */
    public static User getLoginUser() {
        return LOGIN_USER_HOLDER.get();
    }

    /**
     * 娓呯悊褰撳墠绾跨▼涓殑鐧诲綍鐢ㄦ埛锛岄伩鍏嶇嚎绋嬪鐢ㄥ鑷存暟鎹覆鎵般€?     */
    public static void clear() {
        LOGIN_USER_HOLDER.remove();
    }
}



