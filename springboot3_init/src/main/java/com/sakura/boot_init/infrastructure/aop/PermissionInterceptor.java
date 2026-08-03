package com.sakura.boot_init.infrastructure.aop;

import com.sakura.boot_init.shared.annotation.RequirePermission;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限码鉴权切面。
 *
 * <p>校验登录用户是否具备接口标注的权限码；超管角色直接放行。
 * 权限码为空（配置错误）时按拒绝处理，避免鉴权注解失效导致接口被误放行。
 *
 * @author sakura
 */
@Aspect
@Component
public class PermissionInterceptor {

    /**
     * 对带有权限码注解的方法做权限校验。
     *
     * @param joinPoint 切点
     * @param requirePermission 权限码注解
     * @return 原方法执行结果
     * @throws Throwable 执行异常
     */
    @Around("@annotation(requirePermission)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        LoginUserInfo loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (loginUser.isSuperadmin()) {
            return joinPoint.proceed();
        }
        String requiredPermission = requirePermission.value();
        if (StringUtils.isBlank(requiredPermission)) {
            // 配置的权限码为空时必须默认拒绝，避免鉴权注解失效导致接口被误放行。
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (!loginUser.permissions().contains(requiredPermission)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}
