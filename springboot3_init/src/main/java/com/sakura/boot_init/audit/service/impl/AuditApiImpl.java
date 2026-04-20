package com.sakura.boot_init.audit.service.impl;

import com.sakura.boot_init.audit.api.AuditApi;
import com.sakura.boot_init.audit.api.LoginAuditCommand;
import com.sakura.boot_init.audit.api.LoginAuditSubmittedEvent;
import com.sakura.boot_init.audit.model.dto.AuditLogCreateRequest;
import com.sakura.boot_init.audit.service.AuditLogService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 审计模块对外 API 实现，隔离调用方和审计内部模型。
 *
 * @author Sakura
 */
@Component
public class AuditApiImpl implements AuditApi {

    /**
     * 审计日志服务。
     */
    private final AuditLogService auditLogService;

    /**
     * Spring 事件发布器。
     */
    private final ApplicationEventPublisher eventPublisher;

    public AuditApiImpl(AuditLogService auditLogService, ApplicationEventPublisher eventPublisher) {
        this.auditLogService = auditLogService;
        this.eventPublisher = eventPublisher;
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
        eventPublisher.publishEvent(new LoginAuditSubmittedEvent(command));
    }
}
