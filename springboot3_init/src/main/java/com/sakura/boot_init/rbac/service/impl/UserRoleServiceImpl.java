package com.sakura.boot_init.rbac.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.entity.SysRole;
import com.sakura.boot_init.rbac.model.entity.SysUserRole;
import com.sakura.boot_init.rbac.repository.SysRoleMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.UserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.sakura.boot_init.rbac.model.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.sakura.boot_init.rbac.model.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

/**
 * 用户-角色分配服务实现。
 *
 * @author sakura
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

    /**
     * 默认普通用户角色标识。
     */
    private static final String DEFAULT_USER_ROLE_CODE = "user";

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final LoginUserCache loginUserCache;

    public UserRoleServiceImpl(SysUserRoleMapper sysUserRoleMapper, SysRoleMapper sysRoleMapper,
            LoginUserCache loginUserCache) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.loginUserCache = loginUserCache;
    }

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.selectListByQuery(
                        QueryWrapper.create().where(SYS_USER_ROLE.USER_ID.eq(userId)))
                .stream().map(SysUserRole::getRoleId).distinct().toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(Long userId, List<Long> roleIds) {
        // 整体覆盖：先清空该用户的角色关联，再批量插入新的。
        sysUserRoleMapper.deleteByQuery(
                QueryWrapper.create().where(SYS_USER_ROLE.USER_ID.eq(userId)));
        List<Long> safeIds = roleIds == null ? new ArrayList<>() : roleIds.stream().distinct().toList();
        for (Long roleId : safeIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setIsDelete(0);
            sysUserRoleMapper.insert(relation);
        }
        // 用户角色变更后刷新其登录快照缓存，权限即时生效。
        loginUserCache.evict(userId);
        return true;
    }

    @Override
    public void ensureDefaultRole(Long userId) {
        List<SysUserRole> existing = sysUserRoleMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_USER_ROLE.USER_ID.eq(userId)));
        if (!existing.isEmpty()) {
            return;
        }
        SysRole userRole = sysRoleMapper.selectOneByQuery(
                QueryWrapper.create().where(SYS_ROLE.ROLE_CODE.eq(DEFAULT_USER_ROLE_CODE))
                        .and(SYS_ROLE.STATUS.eq(1)).and(SYS_ROLE.IS_DELETE.eq(0)));
        if (userRole == null) {
            return;
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(userRole.getId());
        relation.setIsDelete(0);
        sysUserRoleMapper.insert(relation);
    }
}
