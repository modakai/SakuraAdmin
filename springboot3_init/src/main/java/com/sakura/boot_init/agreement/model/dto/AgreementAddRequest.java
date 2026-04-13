package com.sakura.boot_init.agreement.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增协议请求。
 *
 * @author Sakura
 */
@Data
public class AgreementAddRequest implements Serializable {

    /**
     * 协议类型编码。
     */
    @NotBlank(message = "协议类型不能为空")
    private String agreementType;

    /**
     * 协议标题。
     */
    @NotBlank(message = "协议标题不能为空")
    private String title;

    /**
     * 协议富文本 HTML 内容。
     */
    @NotBlank(message = "协议内容不能为空")
    private String content;

    /**
     * 状态：1 启用，0 禁用。
     */
    @Min(value = 0, message = "协议状态非法")
    @Max(value = 1, message = "协议状态非法")
    private Integer status;

    /**
     * 排序值。
     */
    private Integer sortOrder;

    /**
     * 备注。
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}
