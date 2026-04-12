package com.sakura.boot_init.user.controller.app;

import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.config.WxOpenConfig;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.service.AuthService;
import com.sakura.boot_init.support.annotation.NoLoginRequired;
import com.sakura.boot_init.user.model.dto.UserLoginRequest;
import com.sakura.boot_init.user.model.dto.UserRegisterRequest;
import com.sakura.boot_init.user.model.vo.LoginUserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 鐢ㄦ埛绔璇佹帴鍙ｃ€? *
 * 浣滆€咃細Sakura
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Validated
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private WxOpenConfig wxOpenConfig;

    /**
     * 鐢ㄦ埛娉ㄥ唽銆?     *
     * @param userRegisterRequest 鐢ㄦ埛娉ㄥ唽璇锋眰
     * @return 鏂扮敤鎴?id
     */
    @PostMapping("/register")
    @NoLoginRequired
    public BaseResponse<Long> userRegister(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = authService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 鐢ㄦ埛鐧诲綍銆?     *
     * @param userLoginRequest 鐢ㄦ埛鐧诲綍璇锋眰
     * @param request HTTP 璇锋眰
     * @return 鐧诲綍鐢ㄦ埛淇℃伅鍜?token
     */
    @PostMapping("/login")
    @NoLoginRequired
    public BaseResponse<LoginUserVO> userLogin(@Valid @RequestBody UserLoginRequest userLoginRequest,
            HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = authService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 寰俊寮€鏀惧钩鍙扮櫥褰曘€?     *
     * @param request HTTP 璇锋眰
     * @param response HTTP 鍝嶅簲
     * @param code 寰俊鎺堟潈 code
     * @return 鐧诲綍鐢ㄦ埛淇℃伅鍜?token
     */
    @GetMapping("/login/wx_open")
    @NoLoginRequired
    public BaseResponse<LoginUserVO> userLoginByWxOpen(HttpServletRequest request, HttpServletResponse response,
            @RequestParam("code") @NotBlank(message = "寰俊鎺堟潈 code 涓嶈兘涓虹┖") String code) {
        WxOAuth2AccessToken accessToken;
        try {
            WxMpService wxService = wxOpenConfig.getWxMpService();
            accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, code);
            String unionId = userInfo.getUnionId();
            String mpOpenId = userInfo.getOpenid();
            if (StringUtils.isAnyBlank(unionId, mpOpenId)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
            }
            return ResultUtils.success(authService.userLoginByMpOpen(userInfo, request));
        } catch (Exception e) {
            log.error("userLoginByWxOpen error", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，系统错误");
        }
    }

    /**
     * 鐢ㄦ埛娉ㄩ攢銆?     *
     * @param request HTTP 璇锋眰
     * @return 鏄惁娉ㄩ攢鎴愬姛
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = authService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛銆?     *
     * @param request HTTP 璇锋眰
     * @return 褰撳墠鐧诲綍鐢ㄦ埛淇℃伅
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = authService.getLoginUser(request);
        return ResultUtils.success(authService.getLoginUserVO(user));
    }
}



