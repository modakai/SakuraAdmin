package com.sakura.boot_init.notification.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.notification.model.entity.NotificationRead;
import com.sakura.boot_init.notification.repository.NotificationReadMapper;
import com.sakura.boot_init.notification.service.NotificationReadService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 通知阅读状态服务实现。
 *
 * @author Sakura
 */
@Service
public class NotificationReadServiceImpl extends ServiceImpl<NotificationReadMapper, NotificationRead>
        implements NotificationReadService {

    @Override
    public void markRead(Long notificationId, String receiverType, Long userId) {
        NotificationRead read = getReadRecord(notificationId, receiverType, userId);
        if (read == null) {
            read = buildReadRecord(notificationId, receiverType, userId);
        }
        read.setReadTime(new Date());
        this.saveOrUpdate(read);
    }

    @Override
    public void markClosed(Long notificationId, String receiverType, Long userId) {
        NotificationRead read = getReadRecord(notificationId, receiverType, userId);
        if (read == null) {
            read = buildReadRecord(notificationId, receiverType, userId);
        }
        Date now = new Date();
        read.setCloseTime(now);
        if (read.getReadTime() == null) {
            read.setReadTime(now);
        }
        this.saveOrUpdate(read);
    }

    @Override
    public boolean isRead(Long notificationId, String receiverType, Long userId) {
        NotificationRead read = getReadRecord(notificationId, receiverType, userId);
        return read != null && read.getReadTime() != null;
    }

    @Override
    public boolean isClosed(Long notificationId, String receiverType, Long userId) {
        NotificationRead read = getReadRecord(notificationId, receiverType, userId);
        return read != null && read.getCloseTime() != null;
    }

    /**
     * 获取阅读记录。
     */
    private NotificationRead getReadRecord(Long notificationId, String receiverType, Long userId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("notification_id", notificationId)
                .eq("receiver_type", receiverType)
                .eq("user_id", userId);
        return this.getOne(queryWrapper);
    }

    /**
     * 构造阅读记录。
     */
    private NotificationRead buildReadRecord(Long notificationId, String receiverType, Long userId) {
        NotificationRead read = new NotificationRead();
        read.setNotificationId(notificationId);
        read.setReceiverType(receiverType);
        read.setUserId(userId);
        return read;
    }
}
