package com.sakura.boot_init.user.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.vo.UserVO;

import java.util.List;

/**
 * 用户服务
 *
 * @author sakura
 * @from sakura
 */
public interface UserService extends IService<User> {

    /**
     * 获取脱敏后的用户信息
     *
     * @param user 用户
     * @return 用户视图
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息
     *
     * @param userList 用户列表
     * @return 用户视图列表
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 当前登录用户修改密码。
     *
     * @param loginUser 当前登录用户
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param checkPassword 确认密码
     * @return 是否修改成功
     */
    boolean updateMyPassword(User loginUser, String oldPassword, String newPassword, String checkPassword);

    /**
     * 将指定用户密码重置为系统默认密码。
     *
     * @param id 用户 id
     * @return 是否重置成功
     */
    boolean resetPassword(Long id);
}
