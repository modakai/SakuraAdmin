package com.sakura.boot_init.dict.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 瀛楀吀绫诲瀷
 *
 * @author sakura
 */
@Data
@Table("sys_dict_type")
public class DictType implements Serializable {

    /**
     * 涓婚敭 id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 瀛楀吀缂栫爜
     */
    private String dictCode;

    /**
     * 瀛楀吀鍚嶇О
     */
    private String dictName;

    /**
     * 鐘舵€侊細1 鍚敤锛? 绂佺敤
     */
    private Integer status;

    /**
     * 澶囨敞
     */
    private String remark;

    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private Date updateTime;

    /**
     * 鏄惁鍒犻櫎
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

    /**
     * 搴忓垪鍖栫増鏈彿
     */
    @Column(ignore = true)
    private static final long serialVersionUID = 1L;
}


