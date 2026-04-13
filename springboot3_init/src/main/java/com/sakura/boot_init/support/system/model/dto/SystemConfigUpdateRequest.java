package com.sakura.boot_init.support.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新系统配置请求。
 */
@Data
public class SystemConfigUpdateRequest implements Serializable {

    /**
     * 配置键。
     */
    @NotBlank(message = "配置键不能为空")
    private String key;

    /**
     * 配置值，使用 JSON 字符串保存。
     */
    @NotBlank(message = "配置值不能为空")
    private String value;

    /**
     * 配置说明。
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
