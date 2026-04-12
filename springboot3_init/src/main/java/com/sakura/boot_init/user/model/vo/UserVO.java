package com.sakura.boot_init.user.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 鐢ㄦ埛瑙嗗浘锛堣劚鏁忥級
 *
 * @author sakura
 * @from sakura
 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
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
     * 鐢ㄦ埛绠€浠?     */
    private String userProfile;

    /**
     * 鐢ㄦ埛瑙掕壊锛歶ser/admin/ban
     */
    private String userRole;

    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}

