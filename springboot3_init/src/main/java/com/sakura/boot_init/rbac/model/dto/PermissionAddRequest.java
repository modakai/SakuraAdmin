package com.sakura.boot_init.rbac.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增权限点请求。
 *
 * @author sakura
 */
@Data
public class PermissionAddRequest implements Serializable {

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
     * 权限码，menu 类型可为空（目录节点）。
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
     * 是否显示：1显示 0隐藏（仅 menu 生效；缺省按显示处理）。
     */
    private Integer visible;

    /**
     * 备注。
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
