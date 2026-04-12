package com.sakura.boot_init.user.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 鐢ㄦ埛鏇存柊涓汉淇℃伅璇锋眰
 *
 * @author Sakura
 */
@Data
public class UserUpdateMyRequest implements Serializable {

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

    private static final long serialVersionUID = 1L;
}


