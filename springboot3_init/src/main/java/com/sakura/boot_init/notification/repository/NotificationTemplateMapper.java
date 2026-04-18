package com.sakura.boot_init.notification.repository;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.notification.model.entity.NotificationTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知模板 Mapper。
 *
 * @author Sakura
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplate> {
}
