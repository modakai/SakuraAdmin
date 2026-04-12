package com.sakura.boot_init.dict.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 瀛楀吀鏄庣粏杩斿洖瀵硅薄
 *
 * @author sakura
 */
@Data
public class DictItemVO implements Serializable {

    /**
     * 涓婚敭 id
     */
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
     * 鐘舵€?     */
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

    private static final long serialVersionUID = 1L;
}


