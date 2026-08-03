package com.sakura.boot_init.rbac.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.model.dto.RoleAddRequest;
import com.sakura.boot_init.rbac.model.dto.RoleQueryRequest;
import com.sakura.boot_init.rbac.model.dto.RoleUpdateRequest;
import com.sakura.boot_init.rbac.model.entity.SysRole;
import com.sakura.boot_init.rbac.model.entity.SysRolePermission;
import com.sakura.boot_init.rbac.model.entity.SysUserRole;
import com.sakura.boot_init.rbac.model.vo.RoleVO;
import com.sakura.boot_init.rbac.repository.SysRoleMapper;
import com.sakura.boot_init.rbac.repository.SysRolePermissionMapper;
import com.sakura.boot_init.rbac.repository.SysUserRoleMapper;
import com.sakura.boot_init.rbac.service.RoleService;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sakura.boot_init.rbac.model.entity.table.SysRolePermissionTableDef.SYS_ROLE_PERMISSION;
import static com.sakura.boot_init.rbac.model.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.sakura.boot_init.rbac.model.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

/**
 * 角色管理服务实现。
 *
 * @author sakura
 */
@Service
public class RoleServiceImpl implements RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final LoginUserCache loginUserCache;

    public RoleServiceImpl(SysRoleMapper sysRoleMapper, SysUserRoleMapper sysUserRoleMapper,
            SysRolePermissionMapper sysRolePermissionMapper, LoginUserCache loginUserCache) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.loginUserCache = loginUserCache;
    }

    @Override
    public long addRole(RoleAddRequest request) {
        SysRole existing = sysRoleMapper.selectOneByQuery(
                QueryWrapper.create().where(SYS_ROLE.ROLE_CODE.eq(request.getRoleCode()))
                        .and(SYS_ROLE.IS_DELETE.eq(0)));
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色标识已存在");
        }
        SysRole role = new SysRole();
        role.setRoleCode(request.getRoleCode());
        role.setRoleName(request.getRoleName());
        role.setIsSuperadmin(0);
        role.setStatus(1);
        role.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        role.setRemark(request.getRemark());
        role.setIsDelete(0);
        sysRoleMapper.insert(role);
        return role.getId();
    }

    @Override
    public boolean updateRole(RoleUpdateRequest request) {
        SysRole role = sysRoleMapper.selectOneById(request.getId());
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        role.setRoleName(request.getRoleName());
        role.setStatus(request.getStatus());
        role.setSortOrder(request.getSortOrder());
        role.setRemark(request.getRemark());
        boolean updated = sysRoleMapper.update(role) > 0;
        if (updated) {
            // 启停用或改名后刷新受影响用户快照。
            evictRoleUsers(request.getId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        SysRole role = sysRoleMapper.selectOneById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        if (Integer.valueOf(1).equals(role.getIsSuperadmin())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超管角色不允许删除");
        }
        // 级联清理用户-角色、角色-权限点关联，再物理删除角色。
        sysUserRoleMapper.deleteByQuery(
                QueryWrapper.create().where(SYS_USER_ROLE.ROLE_ID.eq(id)));
        sysRolePermissionMapper.deleteByQuery(
                QueryWrapper.create().where(SYS_ROLE_PERMISSION.ROLE_ID.eq(id)));
        evictRoleUsers(id);
        return sysRoleMapper.deleteById(id) > 0;
    }

    @Override
    public Page<RoleVO> getRolePage(RoleQueryRequest request) {
        QueryWrapper wrapper = QueryWrapper.create()
                .where(SYS_ROLE.ID.eq(request.getId(), request.getId() != null))
                .and(SYS_ROLE.ROLE_CODE.eq(request.getRoleCode(), StringUtils.isNotBlank(request.getRoleCode())))
                .and(SYS_ROLE.ROLE_NAME.like(request.getRoleName(), StringUtils.isNotBlank(request.getRoleName())))
                .and(SYS_ROLE.STATUS.eq(request.getStatus(), request.getStatus() != null))
                .and(SYS_ROLE.IS_DELETE.eq(0))
                .orderBy(SYS_ROLE.SORT_ORDER, true);
        Page<SysRole> rolePage = sysRoleMapper.paginate(request.getPage() == null ? 1 : request.getPage(),
                request.getPageSize() == null ? 10 : request.getPageSize(), wrapper);
        Page<RoleVO> voPage = new Page<>(rolePage.getPageNumber(), rolePage.getPageSize(), rolePage.getTotalRow());
        voPage.setRecords(rolePage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<RoleVO> listAllRoles() {
        List<SysRole> roles = sysRoleMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_ROLE.STATUS.eq(1)).and(SYS_ROLE.IS_DELETE.eq(0))
                        .orderBy(SYS_ROLE.SORT_ORDER, true));
        return roles.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        List<SysRolePermission> relations = sysRolePermissionMapper.selectListByQuery(
                QueryWrapper.create().where(SYS_ROLE_PERMISSION.ROLE_ID.eq(roleId)));
        return relations.stream().map(SysRolePermission::getPermissionId).distinct().toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = sysRoleMapper.selectOneById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "角色不存在");
        }
        // 整体覆盖：先清空该角色的权限点关联，再批量插入新的。
        sysRolePermissionMapper.deleteByQuery(
                QueryWrapper.create().where(SYS_ROLE_PERMISSION.ROLE_ID.eq(roleId)));
        List<Long> safeIds = permissionIds == null ? new ArrayList<>() : permissionIds.stream().distinct().toList();
        for (Long permissionId : safeIds) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            relation.setIsDelete(0);
            sysRolePermissionMapper.insert(relation);
        }
        evictRoleUsers(roleId);
        return true;
    }

    /**
     * 转换角色实体为视图，并附带已分配权限点 id。
     *
     * @param role 角色实体
     * @return 角色视图
     */
    private RoleVO toVO(SysRole role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setIsSuperadmin(role.getIsSuperadmin());
        vo.setStatus(role.getStatus());
        vo.setSortOrder(role.getSortOrder());
        vo.setRemark(role.getRemark());
        vo.setCreateTime(role.getCreateTime());
        vo.setPermissionIds(getRolePermissionIds(role.getId()));
        return vo;
    }

    /**
     * 刷新角色关联用户的登录快照缓存，使权限变更即时生效。
     *
     * @param roleId 角色 id
     */
    private void evictRoleUsers(Long roleId) {
        List<Long> userIds = sysUserRoleMapper.selectListByQuery(
                        QueryWrapper.create().where(SYS_USER_ROLE.ROLE_ID.eq(roleId)))
                .stream().map(SysUserRole::getUserId).distinct().toList();
        userIds.forEach(loginUserCache::evict);
    }
}
