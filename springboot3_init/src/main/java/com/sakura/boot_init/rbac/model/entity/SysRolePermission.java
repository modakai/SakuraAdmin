package com.sakura.boot_init.rbac.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色-权限点关联
 *
 * @author sakura
 */
@Table("sys_role_permission")
@Data
public class SysRolePermission implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 角色 id
     */
    private Long roleId;

    /**
     * 权限点 id
     */
    private Long permissionId;

    private Date createTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
