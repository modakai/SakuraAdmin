package com.sakura.boot_init.rbac.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 角色视图。
 *
 * @author sakura
 */
@Data
public class RoleVO implements Serializable {

    private Long id;

    /**
     * 角色标识。
     */
    private String roleCode;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 是否超管：1是 0否。
     */
    private Integer isSuperadmin;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 备注。
     */
    private String remark;

    private Date createTime;

    /**
     * 已分配的权限点 id 集合。
     */
    private List<Long> permissionIds;

    private static final long serialVersionUID = 1L;
}
