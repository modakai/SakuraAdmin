package com.sakura.boot_init.dict.model.dto;

import com.sakura.boot_init.support.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 瀛楀吀鏄庣粏鍒嗛〉鏌ヨ璇锋眰
 *
 * @author sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DictItemQueryRequest extends PageRequest implements Serializable {

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
     * 鐘舵€?     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}



