package com.sakura.boot_init.observability.repository;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.observability.model.entity.ObservabilityEvent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 运维观测事件 Mapper。
 *
 * @author Sakura
 */
@Mapper
public interface ObservabilityEventMapper extends BaseMapper<ObservabilityEvent> {
}
