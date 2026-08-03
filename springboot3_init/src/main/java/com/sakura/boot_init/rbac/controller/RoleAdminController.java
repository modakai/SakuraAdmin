package com.sakura.boot_init.rbac.controller;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.rbac.model.dto.RoleAddRequest;
import com.sakura.boot_init.rbac.model.dto.RoleAssignPermissionRequest;
import com.sakura.boot_init.rbac.model.dto.RoleQueryRequest;
import com.sakura.boot_init.rbac.model.dto.RoleUpdateRequest;
import com.sakura.boot_init.rbac.model.vo.RoleVO;
import com.sakura.boot_init.rbac.service.RoleService;
import com.sakura.boot_init.shared.annotation.RequirePermission;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.DeleteRequest;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台角色管理接口。
 *
 * @author sakura
 */
@RestController
@RequestMapping("/role")
@Validated
public class RoleAdminController {

    private final RoleService roleService;

    public RoleAdminController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 新增角色。
     */
    @PostMapping("/add")
    @RequirePermission("system:role:add")
    public BaseResponse<Long> addRole(@Valid @RequestBody RoleAddRequest request) {
        return ResultUtils.success(roleService.addRole(request));
    }

    /**
     * 更新角色。
     */
    @PostMapping("/update")
    @RequirePermission("system:role:update")
    public BaseResponse<Boolean> updateRole(@Valid @RequestBody RoleUpdateRequest request) {
        boolean result = roleService.updateRole(request);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除角色，级联清理关联。
     */
    @PostMapping("/delete")
    @RequirePermission("system:role:delete")
    public BaseResponse<Boolean> deleteRole(@Valid @RequestBody DeleteRequest request) {
        boolean result = roleService.deleteRole(request.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页查询角色。
     */
    @PostMapping("/list/page")
    @RequirePermission("system:role:list")
    public BaseResponse<Page<RoleVO>> listRoleByPage(@Valid @RequestBody RoleQueryRequest request) {
        return ResultUtils.success(roleService.getRolePage(request));
    }

    /**
     * 查询全部启用角色，用于下拉选择。
     */
    @GetMapping("/list/all")
    @RequirePermission("system:role:list")
    public BaseResponse<List<RoleVO>> listAllRoles() {
        return ResultUtils.success(roleService.listAllRoles());
    }

    /**
     * 查询角色已分配的权限点 id 集合。
     */
    @GetMapping("/permissions/{roleId}")
    @RequirePermission("system:role:list")
    public BaseResponse<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return ResultUtils.success(roleService.getRolePermissionIds(roleId));
    }

    /**
     * 给角色分配权限点，整体覆盖保存。
     */
    @PostMapping("/assign-permission")
    @RequirePermission("system:role:assign-permission")
    public BaseResponse<Boolean> assignPermissions(@Valid @RequestBody RoleAssignPermissionRequest request) {
        boolean result = roleService.assignPermissions(request.getRoleId(), request.getPermissionIds());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
