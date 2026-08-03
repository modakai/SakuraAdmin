package com.sakura.boot_init.rbac.service;

import java.util.List;

/**
 * 用户-角色分配服务。
 *
 * @author sakura
 */
public interface UserRoleService {

    /**
     * 查询用户已分配的角色 id 列表。
     *
     * @param userId 用户 id
     * @return 角色 id 列表
     */
    List<Long> getRoleIdsByUserId(Long userId);

    /**
     * 给用户分配角色，整体覆盖保存。
     *
     * @param userId 用户 id
     * @param roleIds 角色 id 列表
     * @return 是否成功
     */
    boolean assignRoles(Long userId, List<Long> roleIds);

    /**
     * 确保用户至少拥有一个默认角色（普通用户）；已有角色时不做任何操作。
     *
     * @param userId 用户 id
     */
    void ensureDefaultRole(Long userId);
}
