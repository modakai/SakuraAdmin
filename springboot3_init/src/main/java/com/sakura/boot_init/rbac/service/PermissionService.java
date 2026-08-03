package com.sakura.boot_init.rbac.service;

import com.sakura.boot_init.rbac.model.dto.PermissionAddRequest;
import com.sakura.boot_init.rbac.model.dto.PermissionUpdateRequest;

/**
 * 权限点管理服务。
 *
 * @author sakura
 */
public interface PermissionService {

    /**
     * 新增权限点。
     *
     * @param request 请求
     * @return 新权限点 id
     */
    long addPermission(PermissionAddRequest request);

    /**
     * 更新权限点。
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean updatePermission(PermissionUpdateRequest request);

    /**
     * 删除权限点；存在子节点或被角色引用时拒绝。
     *
     * @param id 权限点 id
     * @return 是否成功
     */
    boolean deletePermission(Long id);
}
