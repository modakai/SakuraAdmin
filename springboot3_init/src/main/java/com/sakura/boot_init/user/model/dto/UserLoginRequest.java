package com.sakura.boot_init.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 鐢ㄦ埛鐧诲綍璇锋眰
 *
 * @author Sakura
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 鐢ㄦ埛璐﹀彿銆?     */
    @NotBlank(message = "鐢ㄦ埛璐﹀彿涓嶈兘涓虹┖")
    @Size(min = 4, message = "鐢ㄦ埛璐﹀彿闀垮害涓嶈兘灏忎簬 4")
    private String userAccount;

    /**
     * 鐢ㄦ埛瀵嗙爜銆?     */
    @NotBlank(message = "鐢ㄦ埛瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 8, message = "鐢ㄦ埛瀵嗙爜闀垮害涓嶈兘灏忎簬 8")
    private String userPassword;
}


