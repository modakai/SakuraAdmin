package com.sakura.boot_init.audit.repository;

import com.mybatisflex.core.BaseMapper;
import com.sakura.boot_init.audit.model.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper。
 *
 * @author Sakura
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
