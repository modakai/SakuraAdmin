package com.sakura.boot_init.rbac.api;

/**
 * 用户权限查询对外 API，供其他模块按用户 id 加载权限快照。
 */
public interface PermissionQueryApi {

    /**
     * 加载用户权限快照。
     *
     * @param userId 用户 id
     * @return 权限快照，无角色时返回空权限
     */
    UserPermission loadUserPermission(Long userId);
}
