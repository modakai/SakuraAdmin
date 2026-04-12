package com.sakura.boot_init.web.interceptor;

import com.sakura.boot_init.common.ErrorCode;
import com.sakura.boot_init.common.context.LoginUserContext;
import com.sakura.boot_init.common.exception.BusinessException;
import com.sakura.boot_init.infra.auth.TokenManager;
import com.sakura.boot_init.infra.persistence.entity.User;
import com.sakura.boot_init.service.UserService;
import com.sakura.boot_init.web.annotation.NoLoginRequired;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器，负责解析 token 并写入当前请求用户上下文。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Token 管理器。
     */
    private final TokenManager tokenManager;

    /**
     * 用户服务。
     */
    private final UserService userService;

    public LoginInterceptor(TokenManager tokenManager, UserService userService) {
        this.tokenManager = tokenManager;
        this.userService = userService;
    }

    /**
     * 请求进入 Controller 前完成登录校验。
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @return 是否放行
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
     * 请求结束后清理 ThreadLocal。
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @param ex       异常信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserContext.clear();
    }

    /**
     * 判断接口或 Controller 是否允许未登录访问。
     *
     * @param handlerMethod Controller 方法
     * @return 是否放行
     */
    private boolean isNoLoginRequired(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(NoLoginRequired.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoLoginRequired.class);
    }
}
