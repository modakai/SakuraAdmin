package com.sakura.boot_init.agreement.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 更新协议请求。
 *
 * @author Sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AgreementUpdateRequest extends AgreementAddRequest implements Serializable {

    /**
     * 协议 id。
     */
    @NotNull(message = "协议 id 不能为空")
    @Positive(message = "协议 id 必须大于 0")
    private Long id;

    private static final long serialVersionUID = 1L;
}
