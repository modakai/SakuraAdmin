package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 给角色分配权限点请求。
 *
 * @author sakura
 */
@Data
public class RoleAssignPermissionRequest implements Serializable {

    /**
     * 角色 id。
     */
    private Long roleId;

    /**
     * 权限点 id 列表，整体覆盖保存。
     */
    private List<Long> permissionIds;

    private static final long serialVersionUID = 1L;
}
