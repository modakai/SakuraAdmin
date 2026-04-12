package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 鏇存柊瀛楀吀鏄庣粏璇锋眰
 *
 * @author sakura
 */
@Data
public class DictItemUpdateRequest implements Serializable {

    /**
     * 涓婚敭 id
     */
    @NotNull(message = "瀛楀吀鏄庣粏 id 涓嶈兘涓虹┖")
    @Positive(message = "瀛楀吀鏄庣粏 id 蹇呴』澶т簬 0")
    private Long id;

    /**
     * 瀛楀吀绫诲瀷 id
     */
    @NotNull(message = "瀛楀吀绫诲瀷 id 涓嶈兘涓虹┖")
    @Positive(message = "瀛楀吀绫诲瀷 id 蹇呴』澶т簬 0")
    private Long dictTypeId;

    /**
     * 瀛楀吀鏍囩
     */
    @NotBlank(message = "瀛楀吀鏍囩涓嶈兘涓虹┖")
    private String dictLabel;

    /**
     * 瀛楀吀鍊?     */
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    /**
     * 鎺掑簭鍊?     */
    private Integer sortOrder;

    /**
     * 鐘舵€?     */
    @NotNull(message = "字典状态不能为空")
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

    private static final long serialVersionUID = 1L;
}


