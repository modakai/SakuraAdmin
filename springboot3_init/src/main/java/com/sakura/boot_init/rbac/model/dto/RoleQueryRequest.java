package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 角色分页查询请求。
 *
 * @author sakura
 */
@Data
public class RoleQueryRequest implements Serializable {

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
     * 状态：1启用 0禁用。
     */
    private Integer status;

    private Integer page;

    private Integer pageSize;

    private String sortField;

    private String sortOrder;

    private static final long serialVersionUID = 1L;
}
