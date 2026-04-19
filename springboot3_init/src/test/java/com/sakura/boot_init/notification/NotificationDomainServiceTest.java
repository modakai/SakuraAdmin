package com.sakura.boot_init.notification;

import com.sakura.boot_init.notification.enums.NotificationReceiverTypeEnum;
import com.sakura.boot_init.notification.enums.NotificationStatusEnum;
import com.sakura.boot_init.notification.enums.NotificationTargetTypeEnum;
import com.sakura.boot_init.notification.model.dto.NotificationTargetContext;
import com.sakura.boot_init.notification.model.entity.Notification;
import com.sakura.boot_init.notification.service.impl.NotificationDomainServiceImpl;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 通知领域核心行为测试。
 *
 * @author Sakura
 */
class NotificationDomainServiceTest {

    /**
     * 全部接收端通知必须同时命中后台和用户端入口。
     */
    @Test
    void shouldMatchAllReceiverTypeForAdminAndAppUsers() {
        NotificationDomainServiceImpl domainService = new NotificationDomainServiceImpl();
        Notification notification = new Notification();
        notification.setReceiverType(NotificationReceiverTypeEnum.ALL.getValue());
        notification.setTargetType(NotificationTargetTypeEnum.ALL.getValue());
        notification.setStatus(NotificationStatusEnum.PUBLISHED.getValue());

        assertEquals(true, domainService.isVisibleTo(notification, adminContext()));
        assertEquals(true, domainService.isVisibleTo(notification, appContext()));
    }

    /**
     * 后台接收端通知不能进入用户端入口。
     */
    @Test
    void shouldRejectAppUserWhenReceiverTypeIsAdmin() {
        NotificationDomainServiceImpl domainService = new NotificationDomainServiceImpl();
        Notification notification = new Notification();
        notification.setReceiverType(NotificationReceiverTypeEnum.ADMIN.getValue());
        notification.setTargetType(NotificationTargetTypeEnum.ALL.getValue());
        notification.setStatus(NotificationStatusEnum.PUBLISHED.getValue());

        assertEquals(false, domainService.isVisibleTo(notification, appContext()));
    }

    /**
     * 指定角色通知只能命中拥有对应角色的用户。
     */
    @Test
    void shouldMatchRoleTargetsWithinReceiverType() {
        NotificationDomainServiceImpl domainService = new NotificationDomainServiceImpl();
        Notification notification = new Notification();
        notification.setReceiverType(NotificationReceiverTypeEnum.ADMIN.getValue());
        notification.setTargetType(NotificationTargetTypeEnum.ROLE.getValue());
        notification.setStatus(NotificationStatusEnum.PUBLISHED.getValue());

        assertEquals(true, domainService.isVisibleTo(notification, adminContext(), List.of("admin"), List.of()));
        assertEquals(false, domainService.isVisibleTo(notification, adminContext(), List.of("user"), List.of()));
    }

    /**
     * 已撤回通知不能继续展示。
     */
    @Test
    void shouldRejectRevokedNotification() {
        NotificationDomainServiceImpl domainService = new NotificationDomainServiceImpl();
        Notification notification = new Notification();
        notification.setReceiverType(NotificationReceiverTypeEnum.ALL.getValue());
        notification.setTargetType(NotificationTargetTypeEnum.ALL.getValue());
        notification.setStatus(NotificationStatusEnum.REVOKED.getValue());

        assertEquals(false, domainService.isVisibleTo(notification, appContext()));
    }

    /**
     * 已归档或已撤回通知不能再次发布。
     */
    @Test
    void shouldRejectPublishWhenNotificationIsRevokedOrArchived() {
        NotificationDomainServiceImpl domainService = new NotificationDomainServiceImpl();
        Notification notification = new Notification();
        notification.setStatus(NotificationStatusEnum.REVOKED.getValue());

        assertThrows(BusinessException.class, () -> domainService.assertCanPublish(notification));
    }

    /**
     * 构造后台用户上下文。
     */
    private NotificationTargetContext adminContext() {
        return new NotificationTargetContext(NotificationReceiverTypeEnum.ADMIN.getValue(),
                new LoginUserInfo(1L, "admin", "admin"));
    }

    /**
     * 构造用户端用户上下文。
     */
    private NotificationTargetContext appContext() {
        return new NotificationTargetContext(NotificationReceiverTypeEnum.APP.getValue(),
                new LoginUserInfo(2L, "user", "user"));
    }
}
