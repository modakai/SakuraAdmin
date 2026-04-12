package com.sakura.boot_init.user.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.vo.UserVO;

import java.util.List;

/**
 * 鐢ㄦ埛鏈嶅姟
 *
 * @author sakura
 * @from sakura
 */
public interface UserService extends IService<User> {

    /**
     * 鑾峰彇鑴辨晱鐨勭敤鎴蜂俊鎭?     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 鑾峰彇鑴辨晱鐨勭敤鎴蜂俊鎭?     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 鑾峰彇鏌ヨ鏉′欢
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

}



