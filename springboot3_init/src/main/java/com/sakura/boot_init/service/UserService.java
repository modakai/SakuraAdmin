package com.sakura.boot_init.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.infra.persistence.entity.User;
import com.sakura.boot_init.web.dto.user.UserQueryRequest;
import com.sakura.boot_init.web.vo.UserVO;

import java.util.List;

/**
 * 用户服务
 *
 * @author sakura
 * @from sakura
 */
public interface UserService extends IService<User> {

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

}
