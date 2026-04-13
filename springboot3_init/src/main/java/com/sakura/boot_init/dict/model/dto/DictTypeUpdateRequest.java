package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新字典类型请求
 *
 * @author sakura
 */
@Data
public class DictTypeUpdateRequest implements Serializable {

    /**
     * 主键 id
     */
    @NotNull(message = "字典类型 id 不能为空")
    @Positive(message = "字典类型 id 必须大于 0")
    private Long id;

    /**
     * 字典编码
     */
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;

    /**
     * 字典名称
     */
    @NotBlank(message = "字典名称不能为空")
    private String dictName;

    /**
     * 状态
     */
    @NotNull(message = "字典状态不能为空")
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
