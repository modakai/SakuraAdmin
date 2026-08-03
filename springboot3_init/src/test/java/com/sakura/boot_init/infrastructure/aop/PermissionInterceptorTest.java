package com.sakura.boot_init.infrastructure.aop;

import com.sakura.boot_init.shared.annotation.RequirePermission;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 权限码鉴权切面测试，验证未登录/超管/权限匹配/缺权限码/配置错误的处理。
 */
class PermissionInterceptorTest {

    private final PermissionInterceptor interceptor = new PermissionInterceptor();

    @AfterEach
    void tearDown() {
        LoginUserContext.clear();
    }

    @Test
    void shouldRejectWhenNotLoggedIn() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.doInterceptor(joinPoint, requirePermission("system:user:list")));

        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), ex.getCode());
    }

    @Test
    void shouldAllowSuperadmin() throws Throwable {
        LoginUserContext.setLoginUser(new LoginUserInfo(1L, "admin", "超管", "admin",
                List.of("admin"), Set.of(), true));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", interceptor.doInterceptor(joinPoint, requirePermission("system:user:list")));
    }

    @Test
    void shouldAllowWhenPermissionMatched() throws Throwable {
        LoginUserContext.setLoginUser(new LoginUserInfo(2L, "user", "用户", "user",
                List.of("user"), Set.of("system:user:list"), false));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", interceptor.doInterceptor(joinPoint, requirePermission("system:user:list")));
    }

    @Test
    void shouldRejectWhenPermissionMissing() {
        LoginUserContext.setLoginUser(new LoginUserInfo(2L, "user", "用户", "user",
                List.of("user"), Set.of("system:dict:list"), false));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.doInterceptor(joinPoint, requirePermission("system:user:list")));

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    @Test
    void shouldRejectWhenPermissionCodeBlank() {
        LoginUserContext.setLoginUser(new LoginUserInfo(2L, "user", "用户", "user",
                List.of("user"), Set.of(), false));
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> interceptor.doInterceptor(joinPoint, requirePermission("")));

        assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), ex.getCode());
    }

    /**
     * 构造携带指定权限码的注解实例。
     *
     * @param code 权限码
     * @return 权限校验注解
     */
    private RequirePermission requirePermission(String code) {
        RequirePermission annotation = mock(RequirePermission.class);
        when(annotation.value()).thenReturn(code);
        return annotation;
    }
}
