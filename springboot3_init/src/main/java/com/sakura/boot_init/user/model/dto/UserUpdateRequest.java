package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.io.Serializable;

/**
 * 鐢ㄦ埛鏇存柊璇锋眰
 *
 * @author Sakura
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * id
     */
    @NotNull(message = "鐢ㄦ埛 id 涓嶈兘涓虹┖")
    @Positive(message = "鐢ㄦ埛 id 蹇呴』澶т簬 0")
    private Long id;

    /**
     * 鐢ㄦ埛鏄电О
     */
    private String userName;

    /**
     * 鐢ㄦ埛澶村儚
     */
    private String userAvatar;

    /**
     * 绠€浠?     */
    private String userProfile;

    /**
     * 鐢ㄦ埛瑙掕壊锛歶ser/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}


