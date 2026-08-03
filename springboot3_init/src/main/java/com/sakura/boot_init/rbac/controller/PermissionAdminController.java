package com.sakura.boot_init.rbac.controller;

import com.sakura.boot_init.rbac.model.dto.PermissionAddRequest;
import com.sakura.boot_init.rbac.model.dto.PermissionUpdateRequest;
import com.sakura.boot_init.rbac.model.vo.PermissionNodeVO;
import com.sakura.boot_init.rbac.service.PermissionService;
import com.sakura.boot_init.rbac.service.PermissionTreeService;
import com.sakura.boot_init.shared.annotation.RequirePermission;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.DeleteRequest;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台权限点管理接口。
 *
 * @author sakura
 */
@RestController
@RequestMapping("/permission")
public class PermissionAdminController {

    private final PermissionTreeService permissionTreeService;
    private final PermissionService permissionService;

    public PermissionAdminController(PermissionTreeService permissionTreeService,
            PermissionService permissionService) {
        this.permissionTreeService = permissionTreeService;
        this.permissionService = permissionService;
    }

    /**
     * 全部启用的权限点树（分配权限时勾选使用）。
     */
    @GetMapping("/tree")
    @RequirePermission("system:permission:list")
    public BaseResponse<List<PermissionNodeVO>> getPermissionTree() {
        return ResultUtils.success(permissionTreeService.buildFullTree());
    }

    /**
     * 新增权限点。
     */
    @PostMapping("/add")
    @RequirePermission("system:permission:add")
    public BaseResponse<Long> addPermission(@Valid @RequestBody PermissionAddRequest request) {
        return ResultUtils.success(permissionService.addPermission(request));
    }

    /**
     * 更新权限点。
     */
    @PostMapping("/update")
    @RequirePermission("system:permission:update")
    public BaseResponse<Boolean> updatePermission(@Valid @RequestBody PermissionUpdateRequest request) {
        boolean result = permissionService.updatePermission(request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除权限点。
     */
    @PostMapping("/delete")
    @RequirePermission("system:permission:delete")
    public BaseResponse<Boolean> deletePermission(@Valid @RequestBody DeleteRequest request) {
        boolean result = permissionService.deletePermission(request.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
