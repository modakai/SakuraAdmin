package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新角色请求。
 *
 * @author sakura
 */
@Data
public class RoleUpdateRequest implements Serializable {

    /**
     * 角色 id。
     */
    private Long id;

    /**
     * 角色名称。
     */
    private String roleName;

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

    private static final long serialVersionUID = 1L;
}
