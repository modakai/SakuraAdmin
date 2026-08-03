package com.sakura.boot_init.rbac;

import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.dto.RoleAddRequest;
import com.sakura.boot_init.rbac.model.entity.SysRole;
import com.sakura.boot_init.rbac.repository.SysRoleMapper;
import com.sakura.boot_init.rbac.repository.SysRolePermissionMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.impl.RoleServiceImpl;
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
 * 角色管理服务测试：角色标识唯一、超管不可删、删除级联清理、分配权限整体覆盖。
 */
class RoleServiceImplTest {

    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final SysRolePermissionMapper rolePermissionMapper = mock(SysRolePermissionMapper.class);
    private final LoginUserCache loginUserCache = mock(LoginUserCache.class);

    private final RoleServiceImpl service =
            new RoleServiceImpl(roleMapper, userRoleMapper, rolePermissionMapper, loginUserCache);

    @Test
    void shouldRejectDuplicateRoleCode() {
        when(roleMapper.selectOneByQuery(any())).thenReturn(role(1L, "admin", 0));
        RoleAddRequest request = new RoleAddRequest();
        request.setRoleCode("admin");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.addRole(request));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(roleMapper, never()).insert(any());
    }

    @Test
    void shouldInsertRoleWhenCodeUnique() {
        when(roleMapper.selectOneByQuery(any())).thenReturn(null);
        // Mock 不会执行 MyBatis-Flex 的 id 生成，这里模拟插入后回填 id。
        org.mockito.Mockito.doAnswer(invocation -> {
            SysRole role = invocation.getArgument(0);
            role.setId(100L);
            return 1;
        }).when(roleMapper).insert(any(SysRole.class));
        RoleAddRequest request = new RoleAddRequest();
        request.setRoleCode("ops");
        request.setRoleName("运维");
        request.setSortOrder(3);

        long id = service.addRole(request);

        assertEquals(100L, id);
        verify(roleMapper).insert(any(SysRole.class));
    }

    @Test
    void shouldRejectDeletingSuperadmin() {
        when(roleMapper.selectOneById(any())).thenReturn(role(1L, "admin", 1));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.deleteRole(1L));

        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), ex.getCode());
        verify(roleMapper, never()).deleteById(any());
    }

    @Test
    void shouldCascadeDeleteRelations() {
        when(roleMapper.selectOneById(any())).thenReturn(role(2L, "user", 0));
        when(userRoleMapper.selectListByQuery(any())).thenReturn(List.of());
        when(userRoleMapper.deleteByQuery(any())).thenReturn(1);
        when(rolePermissionMapper.deleteByQuery(any())).thenReturn(1);
        when(roleMapper.deleteById(any())).thenReturn(1);

        boolean deleted = service.deleteRole(2L);

        assertTrue(deleted);
        // 级联清理用户-角色与角色-权限点关联。
        verify(userRoleMapper).deleteByQuery(any());
        verify(rolePermissionMapper).deleteByQuery(any());
    }

    @Test
    void shouldOverwriteAssignments() {
        when(roleMapper.selectOneById(any())).thenReturn(role(2L, "user", 0));
        when(userRoleMapper.selectListByQuery(any())).thenReturn(List.of());

        service.assignPermissions(2L, List.of(1L, 2L, 2L, 3L));

        // 先清空旧关联，再插入去重后的 3 条新关联。
        verify(rolePermissionMapper).deleteByQuery(any());
        verify(rolePermissionMapper, org.mockito.Mockito.times(3)).insert(any());
    }

    private SysRole role(Long id, String code, Integer isSuperadmin) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(code);
        role.setIsSuperadmin(isSuperadmin);
        role.setStatus(1);
        role.setSortOrder(0);
        role.setIsDelete(0);
        return role;
    }
}
