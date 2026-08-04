package com.sakura.boot_init.user.service.impl;

import com.sakura.boot_init.infrastructure.auth.LoginUserProvider;
import com.sakura.boot_init.infrastructure.auth.LoginUserCache;
import com.sakura.boot_init.rbac.api.PermissionQueryApi;
import com.sakura.boot_init.rbac.api.UserPermission;
import com.sakura.boot_init.rbac.api.UserRoleApi;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.constant.UserConstant;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.exception.BusinessException;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.repository.UserMapper;
import org.springframework.stereotype.Component;

/**
 * 用户模块提供给认证基础设施使用的登录用户快照加载器。
 */
@Component
public class UserLoginUserProvider implements LoginUserProvider {

    /**
     * 用户数据访问对象。
     */
    private final UserMapper userMapper;

    /**
     * 登录用户快照缓存。
     */
    private final LoginUserCache loginUserCache;

    /**
     * 用户权限查询 API。
     */
    private final PermissionQueryApi permissionQueryService;

    /**
     * 用户角色分配 API。
     */
    private final UserRoleApi userRoleService;

    public UserLoginUserProvider(UserMapper userMapper, LoginUserCache loginUserCache,
            PermissionQueryApi permissionQueryService, UserRoleApi userRoleService) {
        this.userMapper = userMapper;
        this.loginUserCache = loginUserCache;
        this.permissionQueryService = permissionQueryService;
        this.userRoleService = userRoleService;
    }

    /**
     * 根据用户 id 查询用户并校验登录态可用性。
     *
     * @param userId 用户 id
     * @return 登录用户快照
     */
    @Override
    public LoginUserInfo loadLoginUser(Long userId) {
        if (userId == null) {
            return null;
        }
        LoginUserInfo cachedLoginUser = loginUserCache.get(userId);
        if (cachedLoginUser != null) {
            return cachedLoginUser;
        }
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            return null;
        }
        validateUserLoginStatus(user);
        UserPermission permission = permissionQueryService.loadUserPermission(userId);
        if (permission.roles().isEmpty()) {
            // 老用户/新注册用户可能没有角色关联，自动补默认普通用户角色，避免登录后菜单为空。
            userRoleService.ensureDefaultRole(userId);
            permission = permissionQueryService.loadUserPermission(userId);
        }
        LoginUserInfo loginUserInfo = new LoginUserInfo(user.getId(), user.getUserAccount(), user.getUserName(),
                user.getUserRole(), permission.roles(), permission.permissions(), permission.superadmin());
        loginUserCache.put(loginUserInfo);
        return loginUserInfo;
    }

    /**
     * 校验用户是否允许继续访问系统。
     *
     * <p>封禁是账号状态（status），不是角色，因此不再依据 user_role 判断封禁。
     *
     * @param user 用户实体
     */
    private void validateUserLoginStatus(User user) {
        if (UserConstant.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "auth.user.disabled");
        }
    }
}
