package com.sakura.boot_init.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.constant.UserConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.support.util.SqlUtils;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.vo.UserVO;
import com.sakura.boot_init.user.repository.UserMapper;
import com.sakura.boot_init.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.DigestUtils;

/**
 * 用户服务实现
 *
 * @author sakura
 * @from sakura
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Integer status = userQueryRequest.getStatus();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", id, id != null);
        queryWrapper.eq("union_id", unionId, StringUtils.isNotBlank(unionId));
        queryWrapper.eq("mp_open_id", mpOpenId, StringUtils.isNotBlank(mpOpenId));
        queryWrapper.eq("user_role", userRole, StringUtils.isNotBlank(userRole));
        queryWrapper.eq("status", status, status != null);
        queryWrapper.like("user_profile", userProfile, StringUtils.isNotBlank(userProfile));
        queryWrapper.like("user_name", userName, StringUtils.isNotBlank(userName));
        if (SqlUtils.validSortField(sortField)) {
            // MyBatis-Flex 的排序条件需要显式判断后再追加
            queryWrapper.orderBy(sortField, CommonConstant.SORT_ORDER_ASC.equals(sortOrder));
        }
        return queryWrapper;
    }

    @Override
    public boolean updateMyPassword(User loginUser, String oldPassword, String newPassword, String checkPassword) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(StringUtils.isAnyBlank(oldPassword, newPassword, checkPassword), ErrorCode.PARAMS_ERROR,
                "auth.param.blank");
        ThrowUtils.throwIf(oldPassword.equals(newPassword), ErrorCode.PARAMS_ERROR, "auth.password.same");
        ThrowUtils.throwIf(!newPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "auth.password.not_match");

        String encryptedOldPassword = DigestUtils
                .md5DigestAsHex((UserConstant.PASSWORD_SALT + oldPassword).getBytes());
        ThrowUtils.throwIf(!encryptedOldPassword.equals(loginUser.getUserPassword()), ErrorCode.PARAMS_ERROR,
                "auth.password.old.invalid");

        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + newPassword).getBytes()));
        return this.updateById(user);
    }

    @Override
    public boolean resetPassword(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户 id 非法");
        }
        User oldUser = this.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);

        User user = new User();
        user.setId(id);
        // 管理员重置密码时统一恢复到系统默认密码。
        user.setUserPassword(DigestUtils.md5DigestAsHex(
                (UserConstant.PASSWORD_SALT + UserConstant.DEFAULT_PASSWORD).getBytes()));
        return this.updateById(user);
    }
}
