package com.sakura.boot_init.rbac;

import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.dto.PermissionAddRequest;
import com.sakura.boot_init.rbac.model.dto.PermissionUpdateRequest;
import com.sakura.boot_init.rbac.model.entity.SysPermission;
import com.sakura.boot_init.rbac.model.entity.SysRolePermission;
import com.sakura.boot_init.rbac.repository.SysPermissionMapper;
import com.sakura.boot_init.rbac.repository.SysRolePermissionMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.impl.PermissionServiceImpl;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 权限点管理服务测试：权限码唯一、删除时子节点/角色引用保护。
 */
class PermissionServiceImplTest {

    private final SysPermissionMapper permissionMapper = mock(SysPermissionMapper.class);
    private final SysRolePermissionMapper rolePermissionMapper = mock(SysRolePermissionMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final LoginUserCache loginUserCache = mock(LoginUserCache.class);

    private final PermissionServiceImpl service = new PermissionServiceImpl(
            permissionMapper, rolePermissionMapper, userRoleMapper, loginUserCache);

    @Test
    void shouldRejectDuplicatePermissionCode() {
        when(permissionMapper.selectCountByQuery(any())).thenReturn(1L);
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("button");
        request.setTitle("新增");
        request.setPermissionCode("system:user:add");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldInsertPermissionWhenCodeUnique() {
        when(permissionMapper.selectCountByQuery(any())).thenReturn(0L);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysPermission p = invocation.getArgument(0);
            p.setId(200L);
            return 1;
        }).when(permissionMapper).insert(any(SysPermission.class));
        PermissionAddRequest request = new PermissionAddRequest();
        // 顶层菜单合法；用 menu 保持该用例聚焦「权限码唯一时可插入」。
        request.setType("menu");
        request.setTitle("新增");
        request.setPermissionCode("system:user:add");

        long id = service.addPermission(request);

        assertEquals(200L, id);
        verify(permissionMapper).insert(any(SysPermission.class));
    }

    @Test
    void shouldRejectDeleteWhenHasChildren() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(9L)));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePermission(2L));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).deleteById(any());
    }

    @Test
    void shouldRejectDeleteWhenReferencedByRole() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of());
        when(rolePermissionMapper.selectListByQuery(any())).thenReturn(List.of(new SysRolePermission()));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deletePermission(3L));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).deleteById(any());
    }

    @Test
    void shouldDeleteWhenNoChildAndNoReference() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of());
        when(rolePermissionMapper.selectListByQuery(any())).thenReturn(List.of());
        when(permissionMapper.deleteById(any())).thenReturn(1);

        boolean deleted = service.deletePermission(3L);

        assertTrue(deleted);
        verify(permissionMapper).deleteById(any());
    }

    @Test
    void shouldRejectAddWhenParentNotExist() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(9L)));
        PermissionAddRequest request = addRequest(999L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldAcceptAddWhenParentExists() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(9L)));
        org.mockito.Mockito.doAnswer(invocation -> {
            SysPermission p = invocation.getArgument(0);
            p.setId(201L);
            return 1;
        }).when(permissionMapper).insert(any(SysPermission.class));
        PermissionAddRequest request = addRequest(9L);

        long id = service.addPermission(request);

        assertEquals(201L, id);
        verify(permissionMapper).insert(any(SysPermission.class));
    }

    @Test
    void shouldRejectUpdateWhenParentIsSelf() {
        SysPermission current = permission(3L);
        when(permissionMapper.selectOneById(3L)).thenReturn(current);
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(2L), current));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updatePermission(updateRequest(3L, 3L)));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).update(any());
    }

    @Test
    void shouldRejectUpdateWhenParentIsDescendant() {
        SysPermission current = permission(3L);
        SysPermission child = permission(5L);
        child.setParentId(3L);
        when(permissionMapper.selectOneById(3L)).thenReturn(current);
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(2L), current, child));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updatePermission(updateRequest(3L, 5L)));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).update(any());
    }

    @Test
    void shouldAcceptUpdateWhenMovingToTopLevel() {
        SysPermission current = permission(3L);
        current.setParentId(2L);
        when(permissionMapper.selectOneById(3L)).thenReturn(current);
        when(permissionMapper.update(any())).thenReturn(1);

        // 提升到顶层（parentId=0）应放行，无需任何父级查询。
        boolean updated = service.updatePermission(updateRequest(3L, 0L));

        assertTrue(updated);
        verify(permissionMapper).update(any());
    }

    @Test
    void shouldAcceptUpdateWhenParentValid() {
        SysPermission current = permission(3L);
        current.setParentId(2L);
        when(permissionMapper.selectOneById(3L)).thenReturn(current);
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(2L), permission(10L), current));
        when(permissionMapper.update(any())).thenReturn(1);

        boolean updated = service.updatePermission(updateRequest(3L, 10L));

        assertTrue(updated);
        verify(permissionMapper).update(any());
    }

    @Test
    void shouldRejectButtonAtTopLevel() {
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("button");
        request.setTitle("按钮");
        request.setParentId(0L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldRejectApiWithParent() {
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("api");
        request.setTitle("接口");
        request.setParentId(9L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldRejectMenuUnderButtonParent() {
        SysPermission buttonParent = permission(101L);
        buttonParent.setType("button");
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(buttonParent));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("menu");
        request.setTitle("子菜单");
        request.setParentId(101L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldRejectButtonUnderButtonParent() {
        SysPermission buttonParent = permission(101L);
        buttonParent.setType("button");
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(buttonParent));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("button");
        request.setTitle("按钮");
        request.setParentId(101L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldAcceptApiAtTopLevel() {
        org.mockito.Mockito.doAnswer(invocation -> {
            SysPermission p = invocation.getArgument(0);
            p.setId(301L);
            return 1;
        }).when(permissionMapper).insert(any(SysPermission.class));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("api");
        request.setTitle("接口");
        request.setParentId(0L);

        long id = service.addPermission(request);

        assertEquals(301L, id);
        verify(permissionMapper).insert(any(SysPermission.class));
    }

    @Test
    void shouldRejectMenuUnderApiParent() {
        SysPermission apiParent = permission(501L);
        apiParent.setType("api");
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(apiParent));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("menu");
        request.setTitle("子菜单");
        request.setParentId(501L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldRejectButtonUnderApiParent() {
        SysPermission apiParent = permission(501L);
        apiParent.setType("api");
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(apiParent));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("button");
        request.setTitle("按钮");
        request.setParentId(501L);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addPermission(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).insert(any());
    }

    @Test
    void shouldAcceptAddButtonUnderMenu() {
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(3L)));
        org.mockito.Mockito.doAnswer(invocation -> {
            SysPermission p = invocation.getArgument(0);
            p.setId(401L);
            return 1;
        }).when(permissionMapper).insert(any(SysPermission.class));
        PermissionAddRequest request = new PermissionAddRequest();
        request.setType("button");
        request.setTitle("新增按钮");
        request.setParentId(3L);

        long id = service.addPermission(request);

        assertEquals(401L, id);
        verify(permissionMapper).insert(any(SysPermission.class));
    }

    @Test
    void shouldRejectChangingMenuWithChildrenToNonMenu() {
        SysPermission current = permission(3L);
        current.setParentId(2L);
        SysPermission child = permission(5L);
        child.setParentId(3L);
        when(permissionMapper.selectOneById(3L)).thenReturn(current);
        when(permissionMapper.selectListByQuery(any())).thenReturn(List.of(permission(2L), current, child));
        PermissionUpdateRequest req = updateRequest(3L, 2L);
        req.setType("button");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.updatePermission(req));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(permissionMapper, never()).update(any());
    }

    @Test
    void shouldAcceptButtonRebindToMenu() {
        SysPermission current = permission(101L);
        current.setType("button");
        current.setParentId(3L);
        when(permissionMapper.selectOneById(101L)).thenReturn(current);
        // 第一次查询（父级校验）返回菜单集；第二次查询（改非菜单类型须无子节点）返回空。
        when(permissionMapper.selectListByQuery(any()))
                .thenReturn(List.of(permission(3L), permission(9L)))
                .thenReturn(List.of());
        when(permissionMapper.update(any())).thenReturn(1);
        PermissionUpdateRequest req = updateRequest(101L, 9L);
        req.setType("button");

        boolean updated = service.updatePermission(req);

        assertTrue(updated);
        verify(permissionMapper).update(any());
    }

    private PermissionAddRequest addRequest(Long parentId) {
        PermissionAddRequest req = new PermissionAddRequest();
        req.setType("menu");
        req.setTitle("子节点");
        req.setParentId(parentId);
        return req;
    }

    private PermissionUpdateRequest updateRequest(Long id, Long parentId) {
        PermissionUpdateRequest req = new PermissionUpdateRequest();
        req.setId(id);
        req.setParentId(parentId);
        req.setType("menu");
        req.setTitle("节点");
        return req;
    }

    private SysPermission permission(Long id) {
        SysPermission p = new SysPermission();
        p.setId(id);
        p.setParentId(0L);
        p.setType("menu");
        p.setTitle("节点");
        p.setStatus(1);
        p.setIsDelete(0);
        return p;
    }
}
