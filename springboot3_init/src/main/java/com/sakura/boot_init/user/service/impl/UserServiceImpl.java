package com.sakura.boot_init.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.util.SqlUtils;
import com.sakura.boot_init.user.repository.UserMapper;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.service.UserService;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.vo.UserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 鐢ㄦ埛鏈嶅姟瀹炵幇
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "璇锋眰鍙傛暟涓虹┖");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", id, id != null);
        queryWrapper.eq("union_id", unionId, StringUtils.isNotBlank(unionId));
        queryWrapper.eq("mp_open_id", mpOpenId, StringUtils.isNotBlank(mpOpenId));
        queryWrapper.eq("user_role", userRole, StringUtils.isNotBlank(userRole));
        queryWrapper.like("user_profile", userProfile, StringUtils.isNotBlank(userProfile));
        queryWrapper.like("user_name", userName, StringUtils.isNotBlank(userName));
        if (SqlUtils.validSortField(sortField)) {
            // MyBatis-Flex 鐨勬帓搴忔潯浠堕渶瑕佹樉寮忓垽鏂悗鍐嶈拷鍔犮€?            queryWrapper.orderBy(sortField, CommonConstant.SORT_ORDER_ASC.equals(sortOrder));
        }
        return queryWrapper;
    }
}



