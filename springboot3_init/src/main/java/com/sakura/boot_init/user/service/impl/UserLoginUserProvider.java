package com.sakura.boot_init.user.service.impl;

import com.sakura.boot_init.infrastructure.auth.LoginUserProvider;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.constant.UserConstant;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.enums.UserRoleEnum;
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

    public UserLoginUserProvider(UserMapper userMapper) {
        this.userMapper = userMapper;
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
        User user = userMapper.selectOneById(userId);
        if (user == null) {
            return null;
        }
        validateUserLoginStatus(user);
        return new LoginUserInfo(user.getId(), user.getUserAccount(), user.getUserRole());
    }

    /**
     * 校验用户是否允许继续访问系统。
     *
     * @param user 用户实体
     */
    private void validateUserLoginStatus(User user) {
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "auth.user.banned");
        }
        if (UserConstant.STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "auth.user.disabled");
        }
    }
}
