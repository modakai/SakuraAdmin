package com.sakura.boot_init.notification.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.notification.model.dto.NotificationAddRequest;
import com.sakura.boot_init.notification.model.dto.NotificationAutoSendRequest;
import com.sakura.boot_init.notification.model.dto.NotificationQueryRequest;
import com.sakura.boot_init.notification.model.dto.NotificationUpdateRequest;
import com.sakura.boot_init.notification.model.entity.Notification;
import com.sakura.boot_init.notification.model.vo.NotificationVO;
import com.sakura.boot_init.user.model.entity.User;

import java.util.List;

/**
 * 通知公告服务。
 *
 * @author Sakura
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 新增通知。
     */
    Long addNotification(NotificationAddRequest request, User operator);

    /**
     * 更新通知。
     */
    boolean updateNotification(NotificationUpdateRequest request, User operator);

    /**
     * 发布通知。
     */
    boolean publishNotification(Long id, User operator);

    /**
     * 撤回通知。
     */
    boolean revokeNotification(Long id, User operator);

    /**
     * 归档通知。
     */
    boolean archiveNotification(Long id, User operator);

    /**
     * 自动模板发送。
     */
    Long sendByTemplate(NotificationAutoSendRequest request);

    /**
     * 构造管理端查询条件。
     */
    QueryWrapper getQueryWrapper(NotificationQueryRequest request);

    /**
     * 获取可见通知列表。
     */
    List<NotificationVO> listVisibleNotifications(String receiverType, User user, String type);

    /**
     * 获取未读消息数量。
     */
    long countUnreadMessages(String receiverType, User user);

    /**
     * 标记单条已读。
     */
    boolean markRead(Long notificationId, String receiverType, User user);

    /**
     * 标记全部已读。
     */
    boolean markAllRead(String receiverType, User user);

    /**
     * 关闭公告弹窗。
     */
    boolean closeAnnouncement(Long notificationId, String receiverType, User user);

    /**
     * 转换返回对象。
     */
    NotificationVO getNotificationVO(Notification notification);

    /**
     * 转换返回对象列表。
     */
    List<NotificationVO> getNotificationVO(List<Notification> notificationList);
}
