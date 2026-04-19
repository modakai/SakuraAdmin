package com.sakura.boot_init.user.controller.app;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.dto.UserUpdateMyRequest;
import com.sakura.boot_init.user.model.dto.UserUpdatePasswordRequest;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.model.vo.UserVO;
import com.sakura.boot_init.user.service.AuthService;
import com.sakura.boot_init.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端用户接口
 * 作者：Sakura
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private AuthService authService;

    /**
     * 根据 id 获取用户包装类。
     *
     * @param id 用户 id
     * @param request HTTP 请求
     * @return 用户包装信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam @Positive(message = "用户 id 必须大于 0") long id,
            HttpServletRequest request) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户封装列表。
     *
     * @param userQueryRequest 查询参数
     * @param request HTTP 请求
     * @return 分页结果
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@Valid @RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getPage();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotalRow());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 更新个人信息。
     *
     * @param userUpdateMyRequest 个人信息更新请求
     * @param request HTTP 请求
     * @return 是否更新成功
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@Valid @RequestBody UserUpdateMyRequest userUpdateMyRequest,
            HttpServletRequest request) {
        User loginUser = authService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 修改当前登录用户密码。
     *
     * @param userUpdatePasswordRequest 修改密码请求
     * @param request HTTP 请求
     * @return 是否修改成功
     */
    @PostMapping("/password/update")
    public BaseResponse<Boolean> updateMyPassword(@Valid @RequestBody UserUpdatePasswordRequest userUpdatePasswordRequest,
            HttpServletRequest request) {
        User loginUser = authService.getLoginUser(request);
        boolean result = userService.updateMyPassword(loginUser, userUpdatePasswordRequest.getOldPassword(),
                userUpdatePasswordRequest.getNewPassword(), userUpdatePasswordRequest.getCheckPassword());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
