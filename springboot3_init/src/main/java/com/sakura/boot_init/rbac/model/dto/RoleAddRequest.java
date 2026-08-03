package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增角色请求。
 *
 * @author sakura
 */
@Data
public class RoleAddRequest implements Serializable {

    /**
     * 角色标识，唯一。
     */
    private String roleCode;

    /**
     * 角色名称。
     */
    private String roleName;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 备注。
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
