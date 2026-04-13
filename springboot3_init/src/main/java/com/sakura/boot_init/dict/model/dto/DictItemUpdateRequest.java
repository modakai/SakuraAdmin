package com.sakura.boot_init.dict.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新字典明细请求
 *
 * @author sakura
 */
@Data
public class DictItemUpdateRequest implements Serializable {

    /**
     * 主键 id
     */
    @NotNull(message = "字典明细 id 不能为空")
    @Positive(message = "字典明细 id 必须大于 0")
    private Long id;

    /**
     * 字典类型 id
     */
    @NotNull(message = "字典类型 id 不能为空")
    @Positive(message = "字典类型 id 必须大于 0")
    private Long dictTypeId;

    /**
     * 字典标签
     */
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;

    /**
     * 字典值
     */
    @NotBlank(message = "字典值不能为空")
    private String dictValue;

    /**
     * 排序值
     */
    private Integer sortOrder;

    /**
     * 状态
     */
    @NotNull(message = "字典状态不能为空")
    private Integer status;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 扩展 JSON
     */
    private String extJson;

    private static final long serialVersionUID = 1L;
}
