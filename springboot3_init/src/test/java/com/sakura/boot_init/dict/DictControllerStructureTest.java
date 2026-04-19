package com.sakura.boot_init.dict;

import com.sakura.boot_init.shared.constant.UserConstant;
import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.dict.controller.publicapi.DictMappingController;
import com.sakura.boot_init.dict.controller.admin.DictItemController;
import com.sakura.boot_init.dict.controller.admin.DictTypeController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 字典控制器结构测试
 *
 * @author sakura
 */
class DictControllerStructureTest {

    /**
     * 管理端字典类型接口必须加管理员权限校验。
     */
    @Test
    void shouldRequireAdminRoleOnTypeManagementEndpoints() {
        assertAdminRole(DictTypeController.class, "addDictType");
        assertAdminRole(DictTypeController.class, "updateDictType");
        assertAdminRole(DictTypeController.class, "deleteDictType");
        assertAdminRole(DictTypeController.class, "getDictTypeById");
        assertAdminRole(DictTypeController.class, "listDictTypeByPage");
    }

    /**
     * 管理端字典明细接口必须加管理员权限校验。
     */
    @Test
    void shouldRequireAdminRoleOnItemManagementEndpoints() {
        assertAdminRole(DictItemController.class, "addDictItem");
        assertAdminRole(DictItemController.class, "updateDictItem");
        assertAdminRole(DictItemController.class, "deleteDictItem");
        assertAdminRole(DictItemController.class, "getDictItemById");
        assertAdminRole(DictItemController.class, "listDictItemByPage");
    }

    /**
     * 公共映射接口应对外暴露查询能力。
     */
    @Test
    void shouldExposePublicMappingEndpoints() {
        assertNotNull(findMethod(DictMappingController.class, "getDictMap"));
        assertNotNull(findMethod(DictMappingController.class, "getDictMapBatch"));
        assertNotNull(findMethod(DictMappingController.class, "getLabelByCodeAndValue"));
    }

    /**
     * 断言控制器方法包含管理员鉴权。
     */
    private void assertAdminRole(Class<?> controllerClass, String methodName) {
        Method method = findMethod(controllerClass, methodName);
        assertNotNull(method, "缺少控制器方法：" + methodName);
        AuthCheck authCheck = method.getAnnotation(AuthCheck.class);
        assertNotNull(authCheck, "方法缺少 @AuthCheck：" + methodName);
        assertEquals(UserConstant.ADMIN_ROLE, authCheck.mustRole());
    }

    /**
     * 按方法名查找目标方法。
     */
    private Method findMethod(Class<?> controllerClass, String methodName) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}

