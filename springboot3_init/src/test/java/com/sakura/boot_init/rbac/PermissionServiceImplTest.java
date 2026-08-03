package com.sakura.boot_init.rbac;

import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.dto.PermissionAddRequest;
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
        request.setType("button");
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
