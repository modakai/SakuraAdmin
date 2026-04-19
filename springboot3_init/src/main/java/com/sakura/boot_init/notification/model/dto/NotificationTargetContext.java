package com.sakura.boot_init.notification.model.dto;

import com.sakura.boot_init.shared.context.LoginUserInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知可见性判断上下文。
 *
 * @author Sakura
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTargetContext {

    /**
     * 当前访问端：admin/app。
     */
    private String receiverType;

    /**
     * 当前登录用户快照。
     */
    private LoginUserInfo user;
}
