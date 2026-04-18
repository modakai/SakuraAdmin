package com.sakura.boot_init.notification.repository;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.notification.model.entity.NotificationRead;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知已读 Mapper。
 *
 * @author Sakura
 */
@Mapper
public interface NotificationReadMapper extends BaseMapper<NotificationRead> {
}
