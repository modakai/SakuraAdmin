package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 鐢ㄦ埛鍒涘缓璇锋眰
 *
 * @author Sakura
 */
@Data
public class UserAddRequest implements Serializable {

    /**
     * 鐢ㄦ埛鏄电О
     */
    private String userName;

    /**
     * 璐﹀彿
     */
    @NotBlank(message = "鐢ㄦ埛璐﹀彿涓嶈兘涓虹┖")
    @Size(min = 4, message = "鐢ㄦ埛璐﹀彿闀垮害涓嶈兘灏忎簬 4")
    private String userAccount;

    /**
     * 鐢ㄦ埛澶村儚
     */
    private String userAvatar;

    /**
     * 鐢ㄦ埛瑙掕壊: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}


