package com.sakura.boot_init.rbac.controller;

import com.sakura.boot_init.rbac.api.UserRoleApi;
import com.sakura.boot_init.shared.annotation.RequirePermission;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台用户角色分配接口。
 *
 * @author sakura
 */
@RestController
@RequestMapping("/user")
@Validated
public class UserRoleAdminController {

    private final UserRoleApi userRoleService;

    public UserRoleAdminController(UserRoleApi userRoleService) {
        this.userRoleService = userRoleService;
    }

    /**
     * 查询用户已分配的角色 id 集合。
     */
    @GetMapping("/roles/{userId}")
    @RequirePermission("system:user:assign-role")
    public BaseResponse<List<Long>> getRolesByUserId(@PathVariable Long userId) {
        return ResultUtils.success(userRoleService.getRoleIdsByUserId(userId));
    }

    /**
     * 给用户分配角色，整体覆盖保存。
     */
    @PostMapping("/assign-role")
    @RequirePermission("system:user:assign-role")
    public BaseResponse<Boolean> assignRoles(
            @Valid @RequestBody AssignRoleRequest request) {
        boolean result = userRoleService.assignRoles(request.userId(), request.roleIds());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分配角色请求。
     *
     * @param userId 用户 id
     * @param roleIds 角色 id 列表
     */
    public record AssignRoleRequest(@NotNull Long userId, List<Long> roleIds) {
    }
}
