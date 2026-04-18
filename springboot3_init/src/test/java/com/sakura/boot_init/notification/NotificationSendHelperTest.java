package com.sakura.boot_init.notification;

import com.sakura.boot_init.notification.enums.NotificationReceiverTypeEnum;
import com.sakura.boot_init.notification.model.dto.NotificationAutoSendRequest;
import com.sakura.boot_init.notification.service.NotificationService;
import com.sakura.boot_init.notification.support.NotificationSendHelper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 通知发送封装测试。
 *
 * @author Sakura
 */
class NotificationSendHelperTest {

    /**
     * 禁用用户通知应由封装类统一组装模板事件和变量。
     */
    @Test
    void shouldSendUserDisabledNotificationThroughTemplate() {
        NotificationService notificationService = mock(NotificationService.class);
        NotificationSendHelper helper = new NotificationSendHelper(notificationService);

        helper.sendUserDisabledNotification(1001L, "违规发布内容");

        ArgumentCaptor<NotificationAutoSendRequest> captor = ArgumentCaptor.forClass(NotificationAutoSendRequest.class);
        verify(notificationService).sendByTemplate(captor.capture());
        NotificationAutoSendRequest request = captor.getValue();
        assertEquals("user_disabled", request.getEventType());
        assertEquals(NotificationReceiverTypeEnum.APP.getValue(), request.getReceiverType());
        assertEquals(1001L, request.getTargetUserId());
        assertEquals("违规发布内容", request.getVariables().get("reason"));
    }

    /**
     * 禁用原因为空时应统一使用兜底文案。
     */
    @Test
    void shouldUseDefaultReasonWhenDisableReasonIsBlank() {
        NotificationService notificationService = mock(NotificationService.class);
        NotificationSendHelper helper = new NotificationSendHelper(notificationService);

        helper.sendUserDisabledNotification(1001L, " ");

        ArgumentCaptor<NotificationAutoSendRequest> captor = ArgumentCaptor.forClass(NotificationAutoSendRequest.class);
        verify(notificationService).sendByTemplate(captor.capture());
        assertEquals("未填写原因", captor.getValue().getVariables().get("reason"));
    }
}
