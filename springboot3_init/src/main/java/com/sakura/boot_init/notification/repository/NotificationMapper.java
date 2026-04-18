package com.sakura.boot_init.notification.repository;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.notification.model.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知公告 Mapper。
 *
 * @author Sakura
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
