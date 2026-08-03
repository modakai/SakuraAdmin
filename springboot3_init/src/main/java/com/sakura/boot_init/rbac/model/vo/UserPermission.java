package com.sakura.boot_init.rbac.model.vo;

import java.util.List;
import java.util.Set;

/**
 * 用户权限快照：角色标识集合 + 权限码集合 + 是否超管。
 *
 * @param roles 角色标识集合
 * @param permissions 权限码集合
 * @param superadmin 是否超管
 */
public record UserPermission(List<String> roles, Set<String> permissions, boolean superadmin) {

    /**
     * 空权限。
     */
    public static UserPermission empty() {
        return new UserPermission(List.of(), Set.of(), false);
    }
}
