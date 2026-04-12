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
 * 瀛楀吀鏄庣粏
 *
 * @author sakura
 */
@Data
@Table("sys_dict_item")
public class DictItem implements Serializable {

    /**
     * 涓婚敭 id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    /**
     * 瀛楀吀绫诲瀷 id
     */
    private Long dictTypeId;

    /**
     * 瀛楀吀鏍囩
     */
    private String dictLabel;

    /**
     * 瀛楀吀鍊?     */
    private String dictValue;

    /**
     * 鎺掑簭鍊?     */
    private Integer sortOrder;

    /**
     * 鐘舵€侊細1 鍚敤锛? 绂佺敤
     */
    private Integer status;

    /**
     * 鏍囩绫诲瀷
     */
    private String tagType;

    /**
     * 澶囨敞
     */
    private String remark;

    /**
     * 鎵╁睍 JSON
     */
    private String extJson;

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


