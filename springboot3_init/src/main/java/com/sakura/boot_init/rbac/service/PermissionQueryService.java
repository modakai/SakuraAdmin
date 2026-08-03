package com.sakura.boot_init.rbac.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.rbac.model.entity.SysPermission;
import com.sakura.boot_init.rbac.model.entity.SysRole;
import com.sakura.boot_init.rbac.model.entity.SysRolePermission;
import com.sakura.boot_init.rbac.model.entity.SysUserRole;
import com.sakura.boot_init.rbac.model.vo.UserPermission;
import com.sakura.boot_init.rbac.repository.SysPermissionMapper;
import com.sakura.boot_init.rbac.repository.SysRoleMapper;
import com.sakura.boot_init.rbac.repository.SysRolePermissionMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sakura.boot_init.rbac.model.entity.table.SysPermissionTableDef.SYS_PERMISSION;
import static com.sakura.boot_init.rbac.model.entity.table.SysRolePermissionTableDef.SYS_ROLE_PERMISSION;
import static com.sakura.boot_init.rbac.model.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.sakura.boot_init.rbac.model.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

/**
 * 用户权限查询服务：聚合用户-角色-权限点，得到角色标识集合、权限码集合与超管标记。
 *
 * <p>超管返回全部启用的权限码；普通用户返回其角色权限码并集（按角色、权限点去重）。
 *
 * @author sakura
 */
@Service
public class PermissionQueryService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public PermissionQueryService(SysUserRoleMapper sysUserRoleMapper, SysRoleMapper sysRoleMapper,
            SysRolePermissionMapper sysRolePermissionMapper, SysPermissionMapper sysPermissionMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    /**
     * 加载用户权限快照。
     *
     * @param userId 用户 id
     * @return 权限快照，无角色时返回空权限
     */
    public UserPermission loadUserPermission(Long userId) {
        if (userId == null) {
            return UserPermission.empty();
        }
        List<Long> roleIds = findRoleIds(userId);
        if (roleIds.isEmpty()) {
            return UserPermission.empty();
        }
        List<SysRole> roles = findRoles(roleIds);
        if (roles.isEmpty()) {
            return UserPermission.empty();
        }
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).toList();
        boolean superadmin = roles.stream().anyMatch(role -> Integer.valueOf(1).equals(role.getIsSuperadmin()));
        Set<String> permissions = superadmin ? findAllEnabledPermissionCodes() : findRolePermissionCodes(roleIds);
        return new UserPermission(roleCodes, permissions, superadmin);
    }

    /**
     * 查询用户启用的角色 id 列表。
     *
     * @param userId 用户 id
     * @return 角色 id 列表
     */
    private List<Long> findRoleIds(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_USER_ROLE.USER_ID.eq(userId)).and(SYS_USER_ROLE.IS_DELETE.eq(0)));
        return userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
    }

    /**
     * 查询启用的角色列表。
     *
     * @param roleIds 角色 id 列表
     * @return 角色列表
     */
    private List<SysRole> findRoles(List<Long> roleIds) {
        return sysRoleMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_ROLE.ID.in(roleIds)).and(SYS_ROLE.STATUS.eq(1)).and(SYS_ROLE.IS_DELETE.eq(0)));
    }

    /**
     * 查询全部启用的权限码（超管使用）。
     *
     * @return 权限码集合
     */
    private Set<String> findAllEnabledPermissionCodes() {
        List<SysPermission> all = sysPermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_PERMISSION.STATUS.eq(1)).and(SYS_PERMISSION.IS_DELETE.eq(0)));
        return collectPermissionCodes(all);
    }

    /**
     * 查询角色拥有的权限码并集（多角色去重）。
     *
     * @param roleIds 角色 id 列表
     * @return 权限码集合
     */
    private Set<String> findRolePermissionCodes(List<Long> roleIds) {
        List<Long> permissionIds = sysRolePermissionMapper.selectListByQuery(
                        QueryWrapper.create().where(SYS_ROLE_PERMISSION.ROLE_ID.in(roleIds))
                                .and(SYS_ROLE_PERMISSION.IS_DELETE.eq(0)))
                .stream().map(SysRolePermission::getPermissionId).distinct().toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }
        List<SysPermission> permissions = sysPermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_PERMISSION.ID.in(permissionIds))
                        .and(SYS_PERMISSION.STATUS.eq(1)).and(SYS_PERMISSION.IS_DELETE.eq(0)));
        return collectPermissionCodes(permissions);
    }

    /**
     * 提取非空权限码集合。
     *
     * @param permissions 权限点列表
     * @return 权限码集合
     */
    private Set<String> collectPermissionCodes(List<SysPermission> permissions) {
        return permissions.stream()
                .map(SysPermission::getPermissionCode)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}
