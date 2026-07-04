package com.sakura.boot_init.audit.listener;

import com.sakura.boot_init.audit.model.dto.AuditLogCreateRequest;
import com.sakura.boot_init.audit.service.AuditLogService;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.event.RateLimitRejectedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听限流拒绝事件，并写入业务审计表。
 *
 * @author Sakura
 */
@Component
public class RateLimitRejectedAuditListener {

    /**
     * 审计日志服务。
     */
    private final AuditLogService auditLogService;

    public RateLimitRejectedAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 将限流拒绝事实转换为管理员操作审计日志。
     *
     * @param event 限流拒绝事件
     */
    @EventListener
    public void onRateLimitRejected(RateLimitRejectedEvent event) {
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setUserId(event.userId());
        request.setAccountIdentifier(event.accountIdentifier());
        request.setIpAddress(event.ipAddress());
        request.setClientInfo(event.clientInfo());
        request.setRequestPath(event.requestPath());
        request.setHttpMethod(event.httpMethod());
        request.setTraceId(event.traceId());
        request.setOperationDescription("请求被限流拒绝");
        request.setBusinessModule("限流保护");
        request.setOperationType("rate_limit");
        request.setStatusCode(ErrorCode.TOO_MANY_REQUESTS.getCode());
        request.setFailureReason("请求过于频繁");
        request.setRequestSummary(buildRequestSummary(event));
        request.setAuditTime(event.occurredAt());
        auditLogService.submitOperationLog(request, false, null, 0);
    }

    /**
     * 构造限流命中摘要，避免审计列表只能看到笼统失败原因。
     */
    private String buildRequestSummary(RateLimitRejectedEvent event) {
        return "key=" + event.rateLimitKey()
                + ", prefix=" + event.rulePrefix()
                + ", dimension=" + event.dimension()
                + ", current=" + event.current()
                + ", limit=" + event.limit()
                + ", ttlSeconds=" + event.ttlSeconds();
    }
}
