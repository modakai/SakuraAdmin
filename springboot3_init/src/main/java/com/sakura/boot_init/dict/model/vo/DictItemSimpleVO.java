package com.sakura.boot_init.dict.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 瀛楀吀鏄犲皠鏈€灏忚繑鍥炲璞? *
 * @author sakura
 */
@Data
public class DictItemSimpleVO implements Serializable {

    /**
     * 鏍囩
     */
    private String label;

    /**
     * 鍊?     */
    private String value;

    /**
     * 鏍囩鏍峰紡绫诲瀷
     */
    private String tagType;

    /**
     * 鎺掑簭鍊?     */
    private Integer sortOrder;

    /**
     * 鎵╁睍 JSON
     */
    private String extJson;

    private static final long serialVersionUID = 1L;
}


