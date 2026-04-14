package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author Sakura
 */
@Data
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    @NotNull(message = "{validation.user.id.not_null}")
    @Positive(message = "{validation.user.id.positive}")
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色，user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
