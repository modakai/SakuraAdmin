package com.sakura.boot_init.dict.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 瀛楀吀绫诲瀷杩斿洖瀵硅薄
 *
 * @author sakura
 */
@Data
public class DictTypeVO implements Serializable {

    /**
     * 涓婚敭 id
     */
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
     * 鐘舵€?     */
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

    private static final long serialVersionUID = 1L;
}


