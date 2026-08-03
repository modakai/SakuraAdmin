package com.sakura.boot_init.shared.context;

import java.util.List;
import java.util.Set;

/**
 * 当前请求登录用户快照，只保留跨模块通用身份字段。
 *
 * @param userId 用户 id
 * @param userAccount 用户账号
 * @param userName 用户昵称
 * @param userRole 用户角色（兼容保留，新代码请使用 roles）
 * @param roles 角色标识集合
 * @param permissions 权限码集合
 * @param isSuperadmin 是否超管
 */
public record LoginUserInfo(
        Long userId,
        String userAccount,
        String userName,
        String userRole,
        List<String> roles,
        Set<String> permissions,
        boolean isSuperadmin) {

    /**
     * 兼容旧调用的构造器，权限数据为空，由登录加载流程填充。
     *
     * @param userId 用户 id
     * @param userAccount 用户账号
     * @param userName 用户昵称
     * @param userRole 用户角色
     */
    public LoginUserInfo(Long userId, String userAccount, String userName, String userRole) {
        this(userId, userAccount, userName, userRole, List.of(), Set.of(), false);
    }
}
