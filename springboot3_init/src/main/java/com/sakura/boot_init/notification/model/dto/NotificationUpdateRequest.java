package com.sakura.boot_init.notification.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新通知公告请求。
 *
 * @author Sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationUpdateRequest extends NotificationAddRequest {

    /**
     * 通知 id。
     */
    @NotNull(message = "通知 id 不能为空")
    private Long id;

    private static final long serialVersionUID = 1L;
}
