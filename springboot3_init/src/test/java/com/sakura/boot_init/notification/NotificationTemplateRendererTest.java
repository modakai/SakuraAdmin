package com.sakura.boot_init.notification;

import com.sakura.boot_init.notification.model.entity.NotificationTemplate;
import com.sakura.boot_init.notification.service.impl.NotificationTemplateRenderer;
import com.sakura.boot_init.support.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 消息通知模板渲染测试。
 *
 * @author Sakura
 */
class NotificationTemplateRendererTest {

    /**
     * 模板中的 reason 占位符必须替换为实际禁用原因。
     */
    @Test
    void shouldRenderReasonVariable() {
        NotificationTemplateRenderer renderer = new NotificationTemplateRenderer();
        NotificationTemplate template = new NotificationTemplate();
        template.setVariableSchema("[{\"name\":\"reason\",\"required\":true}]");
        template.setContentTemplate("您好，您的账户被系统封禁，具体原因{reason}，如有疑虑可联系平台");

        String content = renderer.render(template.getContentTemplate(), template.getVariableSchema(),
                Map.of("reason", "违规发布内容"));

        assertEquals("您好，您的账户被系统封禁，具体原因违规发布内容，如有疑虑可联系平台", content);
    }

    /**
     * 缺少必填变量时不能创建模糊通知。
     */
    @Test
    void shouldRejectMissingRequiredVariable() {
        NotificationTemplateRenderer renderer = new NotificationTemplateRenderer();
        NotificationTemplate template = new NotificationTemplate();
        template.setVariableSchema("[{\"name\":\"reason\",\"required\":true}]");

        assertThrows(BusinessException.class,
                () -> renderer.render("原因：{reason}", template.getVariableSchema(), Map.of()));
    }

    /**
     * 未声明的占位符必须被拦截，避免拼写错误进入线上模板。
     */
    @Test
    void shouldRejectUnknownPlaceholder() {
        NotificationTemplateRenderer renderer = new NotificationTemplateRenderer();
        NotificationTemplate template = new NotificationTemplate();
        template.setVariableSchema("[{\"name\":\"reason\",\"required\":true}]");

        assertThrows(BusinessException.class,
                () -> renderer.render("原因：{banReason}", template.getVariableSchema(), Map.of("reason", "违规")));
    }
}
