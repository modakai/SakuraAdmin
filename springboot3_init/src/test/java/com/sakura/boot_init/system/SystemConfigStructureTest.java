package com.sakura.boot_init.system;

import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.shared.constant.UserConstant;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 系统配置接口结构测试。
 */
class SystemConfigStructureTest {

    /**
     * 系统配置读写接口必须存在，并限制管理员访问。
     */
    @Test
    void shouldExposeAdminConfigEndpoints() {
        assertAdminRole("getConfigByKey");
        assertAdminRole("addConfig");
        assertAdminRole("updateConfig");
    }

    /**
     * 断言目标方法具备管理员鉴权。
     */
    private void assertAdminRole(String methodName) {
        Method method = findMethod(methodName);
        assertNotNull(method, "缺少系统配置接口方法：" + methodName);
        AuthCheck authCheck = method.getAnnotation(AuthCheck.class);
        assertNotNull(authCheck, "系统配置接口缺少管理员鉴权：" + methodName);
        assertEquals(UserConstant.ADMIN_ROLE, authCheck.mustRole());
    }

    /**
     * 根据方法名查找控制器方法。
     */
    private Method findMethod(String methodName) {
        Class<?> controllerClass = getControllerClass();
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 反射加载控制器，确保测试在类缺失时也能给出明确失败原因。
     */
    private Class<?> getControllerClass() {
        try {
            return Class.forName("com.sakura.boot_init.system.controller.SystemConfigController");
        } catch (ClassNotFoundException e) {
            fail("缺少系统配置控制器：com.sakura.boot_init.system.controller.SystemConfigController");
            return null;
        }
    }
}
