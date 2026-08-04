package com.sakura.boot_init.rbac.api;

import java.util.List;

/**
 * 权限点树构建对外 API，供其他模块构建当前用户的菜单/按钮权限树。
 */
public interface PermissionTreeApi {

    /**
     * 构建当前用户的权限点树。
     *
     * @param userId 用户 id
     * @return 权限点树
     */
    List<PermissionNodeVO> buildTreeForUser(Long userId);

    /**
     * 基于已加载的用户权限快照构建权限点树，避免重复查询。
     *
     * @param permission 用户权限快照
     * @return 权限点树
     */
    List<PermissionNodeVO> buildTreeForPermission(UserPermission permission);
}
