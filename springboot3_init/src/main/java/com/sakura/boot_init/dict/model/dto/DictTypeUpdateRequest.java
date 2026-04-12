package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 鏇存柊瀛楀吀绫诲瀷璇锋眰
 *
 * @author sakura
 */
@Data
public class DictTypeUpdateRequest implements Serializable {

    /**
     * 涓婚敭 id
     */
    @NotNull(message = "瀛楀吀绫诲瀷 id 涓嶈兘涓虹┖")
    @Positive(message = "瀛楀吀绫诲瀷 id 蹇呴』澶т簬 0")
    private Long id;

    /**
     * 瀛楀吀缂栫爜
     */
    @NotBlank(message = "瀛楀吀缂栫爜涓嶈兘涓虹┖")
    private String dictCode;

    /**
     * 瀛楀吀鍚嶇О
     */
    @NotBlank(message = "瀛楀吀鍚嶇О涓嶈兘涓虹┖")
    private String dictName;

    /**
     * 鐘舵€?     */
    @NotNull(message = "字典状态不能为空")
    private Integer status;

    /**
     * 澶囨敞
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}


