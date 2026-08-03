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

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        validateParentId(request.getParentId(), null, request.getType());
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
        permission.setVisible(request.getVisible() == null ? 1 : request.getVisible());
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
        validateParentId(request.getParentId(), request.getId(), request.getType());
        // 节点改为非菜单类型时，其下不能仍有子节点（仅菜单可作父级）。
        if (request.getType() != null && !"menu".equals(request.getType())) {
            List<SysPermission> children = sysPermissionMapper.selectListByQuery(
                    QueryWrapper.create().where(SYS_PERMISSION.PARENT_ID.eq(request.getId()))
                            .and(SYS_PERMISSION.IS_DELETE.eq(0)));
            if (!children.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该权限点仍有子节点，不能改为非菜单类型");
            }
        }
        permission.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        permission.setType(request.getType());
        permission.setTitle(request.getTitle());
        permission.setPermissionCode(StringUtils.isBlank(request.getPermissionCode()) ? null : request.getPermissionCode());
        permission.setPath(request.getPath());
        permission.setComponent(request.getComponent());
        permission.setIcon(request.getIcon());
        permission.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }
        if (request.getVisible() != null) {
            permission.setVisible(request.getVisible());
        }
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
     * 校验父级权限点合法（菜单父级规则，见 CONTEXT.md）：
     * <ul>
     *   <li>菜单 → 父级为菜单或顶层</li>
     *   <li>按钮 → 父级为其绑定的菜单</li>
     *   <li>接口 → 固定顶层</li>
     * </ul>
     * 且父级必须存在、未删除；更新时父级不能是自身或自身的后代（防环）。
     *
     * @param parentId 新父级 id（null/0 表示顶层）
     * @param selfId 当前权限点 id（新增时传 null，仅做存在性与类型校验）
     * @param selfType 当前权限点类型（menu/button/api）
     */
    private void validateParentId(Long parentId, Long selfId, String selfType) {
        boolean topLevel = parentId == null || parentId == 0L;
        if ("api".equals(selfType)) {
            if (!topLevel) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口权限点必须为顶层");
            }
            return;
        }
        if (topLevel) {
            if ("button".equals(selfType)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "按钮权限点必须绑定菜单");
            }
            return;
        }
        List<SysPermission> all = sysPermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_PERMISSION.IS_DELETE.eq(0)));
        SysPermission parent = all.stream().filter(p -> parentId.equals(p.getId())).findFirst().orElse(null);
        if (parent == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父级权限点不存在");
        }
        if (selfType != null && !"menu".equals(parent.getType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "父级必须是菜单");
        }
        if (selfId != null) {
            if (parentId.equals(selfId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父级不能是自己");
            }
            if (collectDescendantIds(selfId, all).contains(parentId)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "父级不能是自己的子节点");
            }
        }
    }

    /**
     * 收集某权限点的全部后代 id（按父级分组后 BFS）。
     *
     * @param rootId 根权限点 id
     * @param all 全部未删除的权限点
     * @return 后代 id 集合（不含 rootId）
     */
    private Set<Long> collectDescendantIds(Long rootId, List<SysPermission> all) {
        Map<Long, List<Long>> byParent = all.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(SysPermission::getParentId,
                        Collectors.mapping(SysPermission::getId, Collectors.toList())));
        Set<Long> descendants = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long id = queue.poll();
            for (Long childId : byParent.getOrDefault(id, List.of())) {
                if (descendants.add(childId)) {
                    queue.add(childId);
                }
            }
        }
        return descendants;
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
