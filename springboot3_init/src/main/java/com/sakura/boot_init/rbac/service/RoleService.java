package com.sakura.boot_init.rbac.service;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.rbac.model.dto.RoleAddRequest;
import com.sakura.boot_init.rbac.model.dto.RoleQueryRequest;
import com.sakura.boot_init.rbac.model.dto.RoleUpdateRequest;
import com.sakura.boot_init.rbac.model.vo.RoleVO;

import java.util.List;

/**
 * 角色管理服务。
 *
 * @author sakura
 */
public interface RoleService {

    /**
     * 新增角色。
     *
     * @param request 请求
     * @return 新角色 id
     */
    long addRole(RoleAddRequest request);

    /**
     * 更新角色。
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean updateRole(RoleUpdateRequest request);

    /**
     * 删除角色，级联清理用户-角色、角色-权限点关联。
     *
     * @param id 角色 id
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 分页查询角色。
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<RoleVO> getRolePage(RoleQueryRequest request);

    /**
     * 查询全部启用角色。
     *
     * @return 角色列表
     */
    List<RoleVO> listAllRoles();

    /**
     * 查询角色已分配的权限点 id 集合。
     *
     * @param roleId 角色 id
     * @return 权限点 id 集合
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 给角色分配权限点，整体覆盖保存。
     *
     * @param roleId 角色 id
     * @param permissionIds 权限点 id 列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);
}
