package com.sakura.boot_init.user.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.support.auth.TokenManager;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.UserConstant;
import com.sakura.boot_init.support.context.LoginUserContext;
import com.sakura.boot_init.support.enums.UserRoleEnum;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.vo.LoginUserVO;
import com.sakura.boot_init.user.repository.UserMapper;
import com.sakura.boot_init.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * 认证服务实现。
 *
 * 作者：Sakura
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    /**
     * 用户数据访问对象。
     */
    private final UserMapper userMapper;

    /**
     * Token 管理器。
     */
    private final TokenManager tokenManager;

    public AuthServiceImpl(UserMapper userMapper, TokenManager tokenManager) {
        this.userMapper = userMapper;
        this.tokenManager = tokenManager;
    }

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            QueryWrapper queryWrapper = QueryWrapper.create().eq("user_account", userAccount);
            long count = userMapper.selectCountByQuery(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }

            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword(userPassword));
            int saveResult = userMapper.insertSelective(user);
            if (saveResult <= 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("user_account", userAccount)
                .eq("user_password", encryptPassword(userPassword));
        User user = userMapper.selectOneByQuery(queryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        return buildLoginUserVOWithToken(user);
    }

    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        synchronized (unionId.intern()) {
            QueryWrapper queryWrapper = QueryWrapper.create().eq("union_id", unionId);
            User user = userMapper.selectOneByQuery(queryWrapper);
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封禁，禁止登录");
            }
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                int result = userMapper.insert(user);
                if (result <= 0) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            return buildLoginUserVOWithToken(user);
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        User currentUser = LoginUserContext.getLoginUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        User user = userMapper.selectOneById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }

    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        User currentUser = LoginUserContext.getLoginUser();
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        return userMapper.selectOneById(currentUser.getId());
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        return isAdmin(LoginUserContext.getLoginUser());
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        String token = tokenManager.resolveToken(request);
        if (tokenManager.getUserId(token) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        tokenManager.removeToken(token);
        LoginUserContext.clear();
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 加密用户密码。
     *
     * @param userPassword 明文密码
     * @return 密文密码
     */
    private String encryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + userPassword).getBytes());
    }

    /**
     * 构建带 token 的登录用户视图。
     *
     * @param user 用户实体
     * @return 登录用户视图
     */
    private LoginUserVO buildLoginUserVOWithToken(User user) {
        String token = tokenManager.generateToken();
        tokenManager.storeToken(user.getId(), token);
        LoginUserVO loginUserVO = getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }
}
