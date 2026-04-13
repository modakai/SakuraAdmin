package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 * @author Sakura
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 用户账号
     */
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 4, message = "用户账号长度不能小于 4")
    private String userAccount;

    /**
     * 用户密码
     */
    @NotBlank(message = "用户密码不能为空")
    @Size(min = 8, message = "用户密码长度不能小于 8")
    private String userPassword;

    /**
     * 校验密码
     */
    @NotBlank(message = "校验密码不能为空")
    @Size(min = 8, message = "校验密码长度不能小于 8")
    private String checkPassword;
}
