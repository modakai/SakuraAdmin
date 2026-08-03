package com.sakura.boot_init.rbac;

import com.sakura.boot_init.rbac.model.entity.SysPermission;
import com.sakura.boot_init.rbac.model.vo.PermissionNodeVO;
import com.sakura.boot_init.rbac.model.vo.UserPermission;
import com.sakura.boot_init.rbac.repository.SysPermissionMapper;
import com.sakura.boot_init.rbac.service.PermissionQueryService;
import com.sakura.boot_init.rbac.service.PermissionTreeService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * 权限点树构建测试：递归组装、排序、按权限过滤、空目录剔除、超管全量。
 */
class PermissionTreeServiceTest {

    private final PermissionTreeService service =
            new PermissionTreeService(mock(SysPermissionMapper.class), mock(PermissionQueryService.class));

    @Test
    void shouldAssembleTreeRecursivelyAndFilterByPermission() {
        List<SysPermission> all = List.of(
                permission(1L, 0L, "menu", "工作台", "dashboard:view", 1),
                permission(2L, 0L, "menu", "系统管理", null, 10),
                permission(3L, 2L, "menu", "用户管理", "system:user:list", 11),
                permission(5L, 2L, "menu", "字典管理", "system:dict:list", 13),
                permission(101L, 3L, "button", "新增用户", "system:user:add", 1));

        UserPermission permission = new UserPermission(List.of("user"),
                Set.of("dashboard:view", "system:user:list", "system:user:add"), false);
        List<PermissionNodeVO> tree = service.buildTree(permission, all);

        assertEquals(2, tree.size());
        PermissionNodeVO dashboard = tree.get(0);
        assertEquals("工作台", dashboard.getTitle());

        PermissionNodeVO system = tree.get(1);
        assertEquals("系统管理", system.getTitle());
        assertEquals(1, system.getChildren().size());
        PermissionNodeVO userMenu = system.getChildren().get(0);
        assertEquals("用户管理", userMenu.getTitle());
        assertEquals(1, userMenu.getChildren().size());
        assertEquals("button", userMenu.getChildren().get(0).getType());
        assertEquals("新增用户", userMenu.getChildren().get(0).getTitle());
    }

    @Test
    void shouldDropEmptyDirectoryWhenNoVisibleChild() {
        List<SysPermission> all = List.of(
                permission(2L, 0L, "menu", "系统设置", null, 30),
                permission(15L, 2L, "menu", "通知公告", "system:notification:list", 31));

        UserPermission permission = new UserPermission(List.of("user"), Set.of(), false);
        List<PermissionNodeVO> tree = service.buildTree(permission, all);

        assertTrue(tree.isEmpty());
    }

    @Test
    void shouldIncludeEverythingForSuperadmin() {
        List<SysPermission> all = List.of(
                permission(1L, 0L, "menu", "工作台", "dashboard:view", 1),
                permission(2L, 0L, "menu", "系统管理", null, 10),
                permission(3L, 2L, "menu", "用户管理", "system:user:list", 11),
                permission(101L, 3L, "button", "新增用户", "system:user:add", 1));

        UserPermission permission = new UserPermission(List.of("admin"), Set.of(), true);
        List<PermissionNodeVO> tree = service.buildTree(permission, all);

        assertEquals(2, tree.size());
        PermissionNodeVO system = tree.get(1);
        assertEquals("系统管理", system.getTitle());
        PermissionNodeVO userMenu = system.getChildren().get(0);
        assertEquals("用户管理", userMenu.getTitle());
        assertEquals(1, userMenu.getChildren().size());
    }

    @Test
    void shouldSortSiblingsBySortOrder() {
        List<SysPermission> all = List.of(
                permission(3L, 0L, "menu", "乙", "sys:b", 20),
                permission(1L, 0L, "menu", "甲", "sys:a", 10));

        UserPermission permission = new UserPermission(List.of("user"),
                Set.of("sys:a", "sys:b"), false);
        List<PermissionNodeVO> tree = service.buildTree(permission, all);

        assertEquals("甲", tree.get(0).getTitle());
        assertEquals("乙", tree.get(1).getTitle());
    }

    @Test
    void shouldFilterButtonWithoutPermission() {
        List<SysPermission> all = List.of(
                permission(3L, 0L, "menu", "用户管理", "system:user:list", 11),
                permission(101L, 3L, "button", "新增用户", "system:user:add", 1));

        UserPermission permission = new UserPermission(List.of("user"),
                Set.of("system:user:list"), false);
        List<PermissionNodeVO> tree = service.buildTree(permission, all);

        PermissionNodeVO userMenu = tree.get(0);
        assertEquals("用户管理", userMenu.getTitle());
        // 新增用户按钮无权限，应被过滤。
        assertTrue(userMenu.getChildren().isEmpty());
    }

    private SysPermission permission(Long id, Long parentId, String type, String title,
            String code, Integer sort) {
        SysPermission p = new SysPermission();
        p.setId(id);
        p.setParentId(parentId);
        p.setType(type);
        p.setTitle(title);
        p.setPermissionCode(code);
        p.setSortOrder(sort);
        return p;
    }
}
