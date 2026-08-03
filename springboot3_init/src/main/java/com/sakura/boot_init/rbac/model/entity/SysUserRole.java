package com.sakura.boot_init.rbac.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户-角色关联
 *
 * @author sakura
 */
@Table("sys_user_role")
@Data
public class SysUserRole implements Serializable {

    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 角色 id
     */
    private Long roleId;

    private Date createTime;

    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
