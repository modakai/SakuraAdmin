package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 鏂板瀛楀吀绫诲瀷璇锋眰
 *
 * @author sakura
 */
@Data
public class DictTypeAddRequest implements Serializable {

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


