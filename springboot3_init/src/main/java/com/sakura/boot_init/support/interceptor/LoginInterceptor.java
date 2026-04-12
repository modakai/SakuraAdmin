package com.sakura.boot_init.support.interceptor;

import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.context.LoginUserContext;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.auth.TokenManager;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.service.UserService;
import com.sakura.boot_init.support.annotation.NoLoginRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鐧诲綍鎷︽埅鍣紝璐熻矗瑙ｆ瀽 token 骞跺啓鍏ュ綋鍓嶈姹傜敤鎴蜂笂涓嬫枃銆? */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Token 绠＄悊鍣ㄣ€?     */
    private final TokenManager tokenManager;

    /**
     * 鐢ㄦ埛鏈嶅姟銆?     */
    private final UserService userService;

    public LoginInterceptor(TokenManager tokenManager, UserService userService) {
        this.tokenManager = tokenManager;
        this.userService = userService;
    }

    /**
     * 璇锋眰杩涘叆 Controller 鍓嶅畬鎴愮櫥褰曟牎楠屻€?     *
     * @param request  璇锋眰瀵硅薄
     * @param response 鍝嶅簲瀵硅薄
     * @param handler  澶勭悊鍣?     * @return 鏄惁鏀捐
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        if (isNoLoginRequired(handlerMethod)) {
            return true;
        }
        String token = tokenManager.resolveToken(request);
        Long userId = tokenManager.getUserId(token);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LoginUserContext.setLoginUser(user);
        return true;
    }

    /**
     * 璇锋眰缁撴潫鍚庢竻鐞?ThreadLocal銆?     *
     * @param request  璇锋眰瀵硅薄
     * @param response 鍝嶅簲瀵硅薄
     * @param handler  澶勭悊鍣?     * @param ex       寮傚父淇℃伅
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }

    /**
     * 鍒ゆ柇鎺ュ彛鎴?Controller 鏄惁鍏佽鏈櫥褰曡闂€?     *
     * @param handlerMethod Controller 鏂规硶
     * @return 鏄惁鏀捐
     */
    private boolean isNoLoginRequired(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(NoLoginRequired.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoLoginRequired.class);
    }
}



