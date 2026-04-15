package com.sakura.boot_init.user.controller.admin;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.support.annotation.AuthCheck;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.DeleteRequest;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.constant.UserConstant;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.user.model.dto.UserAddRequest;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.dto.UserUpdateRequest;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理接口。
 *
 * 作者：Sakura
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserAdminController {

    @Resource
    private UserService userService;

    /**
     * 创建用户。
     *
     * @param userAddRequest 创建请求
     * @param request HTTP 请求
     * @return 新用户 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@Valid @RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);

        // 后台创建用户时，初始化默认密码。
        String defaultPassword = UserConstant.DEFAULT_PASSWORD;
        String encryptPassword = DigestUtils.md5DigestAsHex((UserConstant.PASSWORD_SALT + defaultPassword).getBytes());
        user.setUserPassword(encryptPassword);

        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户。
     *
     * @param deleteRequest 删除请求
     * @param request HTTP 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@Valid @RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户。
     *
     * @param userUpdateRequest 更新请求
     * @param request HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@Valid @RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request) {
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户。
     *
     * @param id 用户 id
     * @param request HTTP 请求
     * @return 用户信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@RequestParam @Positive(message = "用户 id 必须大于 0") long id,
            HttpServletRequest request) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 分页查询用户。
     *
     * @param userQueryRequest 查询请求
     * @param request HTTP 请求
     * @return 分页结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@Valid @RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getPage();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 管理员重置用户密码。
     *
     * @param deleteRequest 重置请求，仅使用用户 id
     * @param request HTTP 请求
     * @return 是否重置成功
     */
    @PostMapping("/reset/password")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> resetUserPassword(@Valid @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest request) {
        boolean result = userService.resetPassword(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
