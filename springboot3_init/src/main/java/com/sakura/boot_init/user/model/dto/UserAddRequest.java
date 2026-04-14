package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author Sakura
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    @NotBlank(message = "{validation.user.account.not_blank}")
    @Size(min = 4, message = "{validation.user.account.min}")
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户角色：user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
