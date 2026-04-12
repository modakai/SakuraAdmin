package com.sakura.boot_init.user.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 宸茬櫥褰曠敤鎴疯鍥撅紙鑴辨晱锛? *
 * @author Sakura
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * 鐢ㄦ埛 id
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
     * 鐧诲綍 token锛屽悗缁姹傞渶瑕佹惡甯﹁鍊笺€?     */
    private String token;

    /**
     * 鍒涘缓鏃堕棿
     */
    private Date createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}



