package com.sakura.boot_init.user.service;

import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.vo.LoginUserVO;
import jakarta.servlet.http.HttpServletRequest;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;

/**
 * 璁よ瘉鏈嶅姟銆? */
public interface AuthService {

    /**
     * 鐢ㄦ埛娉ㄥ唽銆?     *
     * @param userAccount   鐢ㄦ埛璐﹀彿
     * @param userPassword  鐢ㄦ埛瀵嗙爜
     * @param checkPassword 鏍￠獙瀵嗙爜
     * @return 鏂扮敤鎴?id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 鐢ㄦ埛鐧诲綍銆?     *
     * @param userAccount  鐢ㄦ埛璐﹀彿
     * @param userPassword 鐢ㄦ埛瀵嗙爜
     * @param request      HTTP 璇锋眰
     * @return 鐧诲綍鐢ㄦ埛淇℃伅
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 寰俊寮€鏀惧钩鍙扮櫥褰曘€?     *
     * @param wxOAuth2UserInfo 寰俊鎺堟潈鐢ㄦ埛淇℃伅
     * @param request          HTTP 璇锋眰
     * @return 鐧诲綍鐢ㄦ埛淇℃伅
     */
    LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request);

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛銆?     *
     * @param request HTTP 璇锋眰
     * @return 褰撳墠鐧诲綍鐢ㄦ埛
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛锛屽厑璁告湭鐧诲綍銆?     *
     * @param request HTTP 璇锋眰
     * @return 褰撳墠鐧诲綍鐢ㄦ埛锛屾湭鐧诲綍杩斿洖 null
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 鍒ゆ柇褰撳墠璇锋眰鐢ㄦ埛鏄惁涓虹鐞嗗憳銆?     *
     * @param request HTTP 璇锋眰
     * @return 鏄惁涓虹鐞嗗憳
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 鍒ゆ柇鎸囧畾鐢ㄦ埛鏄惁涓虹鐞嗗憳銆?     *
     * @param user 鐢ㄦ埛
     * @return 鏄惁涓虹鐞嗗憳
     */
    boolean isAdmin(User user);

    /**
     * 鐢ㄦ埛娉ㄩ攢銆?     *
     * @param request HTTP 璇锋眰
     * @return 鏄惁娉ㄩ攢鎴愬姛
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 鑾峰彇鑴辨晱鐨勭櫥褰曠敤鎴蜂俊鎭€?     *
     * @param user 鐢ㄦ埛瀹炰綋
     * @return 鐧诲綍鐢ㄦ埛瑙嗗浘
     */
    LoginUserVO getLoginUserVO(User user);
}



