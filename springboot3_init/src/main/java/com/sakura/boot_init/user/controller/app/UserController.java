package com.sakura.boot_init.user.controller.app;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.user.model.entity.User;
import com.sakura.boot_init.user.service.AuthService;
import com.sakura.boot_init.user.service.UserService;
import com.sakura.boot_init.user.model.dto.UserQueryRequest;
import com.sakura.boot_init.user.model.dto.UserUpdateMyRequest;
import com.sakura.boot_init.user.model.vo.UserVO;
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
 * 鐢ㄦ埛绔敤鎴锋帴鍙ｃ€? *
 * 浣滆€咃細Sakura
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
     * 鏍规嵁 id 鑾峰彇鐢ㄦ埛鍖呰绫汇€?     *
     * @param id 鐢ㄦ埛 id
     * @param request HTTP 璇锋眰
     * @return 鐢ㄦ埛鍖呰淇℃伅
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(@RequestParam @Positive(message = "鐢ㄦ埛 id 蹇呴』澶т簬 0") long id,
            HttpServletRequest request) {
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 鍒嗛〉鑾峰彇鐢ㄦ埛灏佽鍒楄〃銆?     *
     * @param userQueryRequest 鏌ヨ鍙傛暟
     * @param request HTTP 璇锋眰
     * @return 鍒嗛〉缁撴灉
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@Valid @RequestBody UserQueryRequest userQueryRequest,
            HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 闄愬埗鐖櫕銆?        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotalRow());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 鏇存柊涓汉淇℃伅銆?     *
     * @param userUpdateMyRequest 涓汉淇℃伅鏇存柊璇锋眰
     * @param request HTTP 璇锋眰
     * @return 鏄惁鏇存柊鎴愬姛
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
}



