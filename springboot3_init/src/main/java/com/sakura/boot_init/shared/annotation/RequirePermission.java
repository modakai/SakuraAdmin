package com.sakura.boot_init.shared.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限码校验，标注接口所需的最小权限点。
 *
 * <p>权限码与权限点表（permission_code）一一对应，如 {@code system:user:list}。
 * 权限码无法解析时按拒绝处理，避免注解配置错误导致接口被误放行。
 *
 * @author sakura
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 必须具备的权限码。
     *
     * @return 权限码
     */
    String value();
}
