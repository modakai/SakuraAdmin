package com.sakura.boot_init.rbac;

import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.entity.SysRole;
import com.sakura.boot_init.rbac.repository.SysRoleMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.impl.UserRoleServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户-角色分配服务测试：覆盖保存、缓存失效、无角色时补默认角色。
 */
class UserRoleServiceImplTest {

    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final LoginUserCache loginUserCache = mock(LoginUserCache.class);

    private final UserRoleServiceImpl service = new UserRoleServiceImpl(userRoleMapper, roleMapper, loginUserCache);

    @Test
    void shouldOverwriteAndEvictCache() {
        service.assignRoles(100L, List.of(1L, 2L, 2L, 3L));

        // 先清空旧关联，再插入去重后的 3 条，并刷新用户缓存。
        verify(userRoleMapper).deleteByQuery(any());
        verify(userRoleMapper, times(3)).insert(any());
        verify(loginUserCache).evict(100L);
        assertTrue(true);
    }

    @Test
    void shouldAssignDefaultRoleWhenUserHasNone() {
        when(userRoleMapper.selectListByQuery(any())).thenReturn(List.of());
        SysRole userRole = new SysRole();
        userRole.setId(2L);
        userRole.setRoleCode("user");
        when(roleMapper.selectOneByQuery(any())).thenReturn(userRole);

        service.ensureDefaultRole(200L);

        verify(userRoleMapper).insert(any());
    }

    @Test
    void shouldNotAssignWhenUserAlreadyHasRole() {
        com.sakura.boot_init.rbac.model.entity.SysUserRole relation =
                new com.sakura.boot_init.rbac.model.entity.SysUserRole();
        relation.setUserId(200L);
        relation.setRoleId(2L);
        when(userRoleMapper.selectListByQuery(any())).thenReturn(List.of(relation));

        service.ensureDefaultRole(200L);

        verify(userRoleMapper, org.mockito.Mockito.never()).insert(any());
    }
}
