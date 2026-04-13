package com.sakura.boot_init.support.common;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除请求
 *
 * @author sakura
 * @from sakura
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    @NotNull(message = "id 不能为空")
    @Positive(message = "id 必须大于 0")
    private Long id;

    private static final long serialVersionUID = 1L;
}
