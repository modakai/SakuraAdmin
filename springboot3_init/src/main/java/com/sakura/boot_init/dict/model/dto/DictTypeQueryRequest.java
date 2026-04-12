package com.sakura.boot_init.dict.model.dto;

import com.sakura.boot_init.support.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 瀛楀吀绫诲瀷鍒嗛〉鏌ヨ璇锋眰
 *
 * @author sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictTypeQueryRequest extends PageRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}



