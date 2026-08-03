package com.sakura.boot_init.rbac.model.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限点树节点，用于登录后下发菜单树与按钮权限。
 *
 * @author sakura
 */
@Data
public class PermissionNodeVO {

    private Long id;

    private Long parentId;

    /**
     * 类型：menu/button/api
     */
    private String type;

    /**
     * 标题
     */
    private String title;

    /**
     * 权限码
     */
    private String permissionCode;

    /**
     * 菜单路由路径（menu）
     */
    private String path;

    /**
     * 组件标识（menu）
     */
    private String component;

    /**
     * 菜单图标（menu）
     */
    private String icon;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 是否显示：1显示 0隐藏（仅 menu 生效；隐藏节点仍下发，由前端侧边栏渲染时裁剪）
     */
    private Integer visible;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 子节点
     */
    private List<PermissionNodeVO> children = new ArrayList<>();
}
