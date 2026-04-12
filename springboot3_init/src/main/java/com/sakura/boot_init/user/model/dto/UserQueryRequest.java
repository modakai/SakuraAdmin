package com.sakura.boot_init.user.model.dto;

import com.sakura.boot_init.support.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 鐢ㄦ埛鏌ヨ璇锋眰
 *
 * @author Sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 寮€鏀惧钩鍙癷d
     */
    private String unionId;

    /**
     * 鍏紬鍙穙penId
     */
    private String mpOpenId;

    /**
     * 鐢ㄦ埛鏄电О
     */
    private String userName;

    /**
     * 绠€浠?     */
    private String userProfile;

    /**
     * 鐢ㄦ埛瑙掕壊锛歶ser/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}



