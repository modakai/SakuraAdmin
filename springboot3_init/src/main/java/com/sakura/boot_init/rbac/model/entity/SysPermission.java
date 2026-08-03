package com.sakura.boot_init.rbac.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 权限点。菜单/按钮/接口三类共用一张表，type 区分。
 *
 * @author sakura
 */
@Table("sys_permission")
@Data
public class SysPermission implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 父权限点 id，0 为根
     */
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
     * 权限码，如 system:user:list
     */
    private String permissionCode;

    /**
     * 菜单路由路径（menu）
     */
    private String path;

    /**
     * 组件标识（menu），对应前端组件映射表
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
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private Date createTime;

    private Date updateTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
