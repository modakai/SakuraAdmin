package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新权限点请求。
 *
 * @author sakura
 */
@Data
public class PermissionUpdateRequest implements Serializable {

    private Long id;

    /**
     * 父权限点 id，0 为根。
     */
    private Long parentId;

    /**
     * 类型：menu/button/api。
     */
    private String type;

    /**
     * 标题。
     */
    private String title;

    /**
     * 权限码。
     */
    private String permissionCode;

    /**
     * 菜单路由路径（menu）。
     */
    private String path;

    /**
     * 组件标识（menu）。
     */
    private String component;

    /**
     * 菜单图标（menu）。
     */
    private String icon;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 状态：1启用 0禁用。
     */
    private Integer status;

    /**
     * 是否显示：1显示 0隐藏（仅 menu 生效）。
     */
    private Integer visible;

    /**
     * 备注。
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
