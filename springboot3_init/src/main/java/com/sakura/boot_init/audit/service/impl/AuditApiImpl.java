package com.sakura.boot_init.audit.service.impl;

import com.sakura.boot_init.audit.api.AuditApi;
import com.sakura.boot_init.audit.api.LoginAuditCommand;
import com.sakura.boot_init.audit.model.dto.AuditLogCreateRequest;
import com.sakura.boot_init.audit.service.AuditLogService;
import org.springframework.stereotype.Service;

/**
 * 审计模块对外 API 实现，封装内部审计 DTO 和服务。
 */
@Service
public class AuditApiImpl implements AuditApi {

    /**
     * 审计日志服务。
     */
    private final AuditLogService auditLogService;

    public AuditApiImpl(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public void submitLoginLog(LoginAuditCommand command) {
        if (command == null) {
            return;
        }
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setUserId(command.userId());
        request.setAccountIdentifier(command.accountIdentifier());
        request.setIpAddress(command.ipAddress());
        request.setClientInfo(command.clientInfo());
        auditLogService.submitLoginLog(request, command.success(), command.failureReason(), command.costMillis());
    }
}
