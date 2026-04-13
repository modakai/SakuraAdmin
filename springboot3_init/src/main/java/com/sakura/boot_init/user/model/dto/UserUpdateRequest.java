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
    @NotNull(message = "用户 id 不能为空")
    @Positive(message = "用户 id 必须大于 0")
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
