package com.sakura.boot_init.notification.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新消息通知模板请求。
 *
 * @author Sakura
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class NotificationTemplateUpdateRequest extends NotificationTemplateAddRequest {

    /**
     * 模板 id。
     */
    @NotNull(message = "模板 id 不能为空")
    private Long id;

    private static final long serialVersionUID = 1L;
}
