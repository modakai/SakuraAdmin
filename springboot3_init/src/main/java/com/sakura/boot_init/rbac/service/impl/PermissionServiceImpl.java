package com.sakura.boot_init.rbac.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.dto.PermissionAddRequest;
import com.sakura.boot_init.rbac.model.dto.PermissionUpdateRequest;
import com.sakura.boot_init.rbac.model.entity.SysPermission;
import com.sakura.boot_init.rbac.model.entity.SysRolePermission;
import com.sakura.boot_init.rbac.model.entity.SysUserRole;
import com.sakura.boot_init.rbac.repository.SysPermissionMapper;
import com.sakura.boot_init.rbac.repository.SysRolePermissionMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.PermissionService;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.sakura.boot_init.rbac.model.entity.table.SysPermissionTableDef.SYS_PERMISSION;
import static com.sakura.boot_init.rbac.model.entity.table.SysRolePermissionTableDef.SYS_ROLE_PERMISSION;
import static com.sakura.boot_init.rbac.model.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

/**
 * 权限点管理服务实现。
 *
 * @author sakura
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final LoginUserCache loginUserCache;

    public PermissionServiceImpl(SysPermissionMapper sysPermissionMapper,
            SysRolePermissionMapper sysRolePermissionMapper, SysUserRoleMapper sysUserRoleMapper,
            LoginUserCache loginUserCache) {
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.loginUserCache = loginUserCache;
    }

    @Override
    public long addPermission(PermissionAddRequest request) {
        validatePermissionCodeUnique(request.getPermissionCode(), null);
        SysPermission permission = new SysPermission();
        permission.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        permission.setType(request.getType());
        permission.setTitle(request.getTitle());
        permission.setPermissionCode(StringUtils.isBlank(request.getPermissionCode()) ? null : request.getPermissionCode());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setIcon(request.getIcon());
        permission.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        permission.setStatus(1);
        permission.setRemark(request.getRemark());
        permission.setIsDelete(0);
        sysPermissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    public boolean updatePermission(PermissionUpdateRequest request) {
        SysPermission permission = sysPermissionMapper.selectOneById(request.getId());
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "权限点不存在");
        }
        validatePermissionCodeUnique(request.getPermissionCode(), request.getId());
        permission.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        permission.setType(request.getType());
        permission.setTitle(request.getTitle());
        permission.setPermissionCode(StringUtils.isBlank(request.getPermissionCode()) ? null : request.getPermissionCode());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setIcon(request.getIcon());
        permission.setSortOrder(request.getSortOrder());
        permission.setStatus(request.getStatus());
        permission.setRemark(request.getRemark());
        boolean updated = sysPermissionMapper.update(permission) > 0;
        if (updated) {
            // 权限点变更后刷新受影响用户快照。
            evictPermissionUsers(request.getId());
        }
        return updated;
    }

    @Override
    public boolean deletePermission(Long id) {
        List<SysPermission> children = sysPermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_PERMISSION.PARENT_ID.eq(id)).and(SYS_PERMISSION.IS_DELETE.eq(0)));
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "存在子权限点，请先删除子节点");
        }
        List<SysRolePermission> references = sysRolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_ROLE_PERMISSION.PERMISSION_ID.eq(id)));
        if (!references.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该权限点已被角色使用，请先解除分配");
        }
        evictPermissionUsers(id);
        return sysPermissionMapper.deleteById(id) > 0;
    }

    /**
     * 校验权限码在其它权限点中唯一。
     *
     * @param permissionCode 权限码
     * @param excludeId 排除的权限点 id
     */
    private void validatePermissionCodeUnique(String permissionCode, Long excludeId) {
        if (StringUtils.isBlank(permissionCode)) {
            return;
        }
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_PERMISSION.PERMISSION_CODE.eq(permissionCode))
                .and(SYS_PERMISSION.IS_DELETE.eq(0))
                .and(SYS_PERMISSION.ID.ne(excludeId, excludeId != null));
        if (sysPermissionMapper.selectCountByQuery(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "权限码已存在");
        }
    }

    /**
     * 刷新持有该权限点的角色的关联用户缓存。
     *
     * @param permissionId 权限点 id
     */
    private void evictPermissionUsers(Long permissionId) {
        List<Long> roleIds = sysRolePermissionMapper.selectListByQuery(
                        QueryWrapper.create().where(SYS_ROLE_PERMISSION.PERMISSION_ID.eq(permissionId)))
                .stream().map(SysRolePermission::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return;
        }
        List<Long> userIds = sysUserRoleMapper.selectListByQuery(
                        QueryWrapper.create().where(SYS_USER_ROLE.ROLE_ID.in(roleIds)))
                .stream().map(SysUserRole::getUserId).distinct().toList();
        userIds.forEach(loginUserCache::evict);
    }
}
