package com.sakura.boot_init.audit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.audit.enums.AuditLogResultEnum;
import com.sakura.boot_init.audit.enums.AuditLogTypeEnum;
import com.sakura.boot_init.audit.model.dto.AuditLogCreateRequest;
import com.sakura.boot_init.audit.model.dto.AuditLogExportRequest;
import com.sakura.boot_init.audit.model.dto.AuditLogQueryRequest;
import com.sakura.boot_init.audit.model.entity.AuditLog;
import com.sakura.boot_init.audit.model.vo.AuditLogVO;
import com.sakura.boot_init.audit.repository.AuditLogMapper;
import com.sakura.boot_init.audit.service.AuditLogService;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.support.util.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现。
 *
 * @author Sakura
 */
@Service
@Slf4j
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements AuditLogService {

    /**
     * 默认最大导出数量。
     */
    private static final int DEFAULT_EXPORT_LIMIT = 5000;

    /**
     * 审计日志 Mapper。
     */
    private final AuditLogMapper auditLogMapper;

    /**
     * 摘要脱敏工具。
     */
    private final AuditSanitizer auditSanitizer;

    /**
     * 审计写入执行器，使用守护线程避免阻塞应用退出。
     */
    private final Executor auditLogExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "audit-log-writer");
        thread.setDaemon(true);
        return thread;
    });

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
        this.auditSanitizer = new AuditSanitizer();
    }

    @Override
    public void submitLoginLog(AuditLogCreateRequest request, boolean success, String failureReason, long costMillis) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        request.setLogType(AuditLogTypeEnum.LOGIN.getValue());
        request.setOperationType("login");
        request.setResult(success ? AuditLogResultEnum.SUCCESS.getValue() : AuditLogResultEnum.FAILURE.getValue());
        request.setFailureReason(failureReason);
        request.setCostMillis(costMillis);
        saveAuditLogSafely(request);
    }

    @Override
    public void submitOperationLog(AuditLogCreateRequest request, boolean success, Throwable throwable, long costMillis) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        request.setLogType(AuditLogTypeEnum.ADMIN_OPERATION.getValue());
        request.setResult(success ? AuditLogResultEnum.SUCCESS.getValue() : AuditLogResultEnum.FAILURE.getValue());
        request.setCostMillis(costMillis);
        if (throwable != null) {
            request.setExceptionSummary(buildExceptionSummary(throwable));
        }
        saveAuditLogSafely(request);
    }

    @Override
    public QueryWrapper getQueryWrapper(AuditLogQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("log_type", request.getLogType(), StringUtils.isNotBlank(request.getLogType()));
        queryWrapper.eq("user_id", request.getUserId(), request.getUserId() != null);
        queryWrapper.like("account_identifier", request.getAccountIdentifier(), StringUtils.isNotBlank(request.getAccountIdentifier()));
        queryWrapper.eq("ip_address", request.getIpAddress(), StringUtils.isNotBlank(request.getIpAddress()));
        queryWrapper.like("request_path", request.getRequestPath(), StringUtils.isNotBlank(request.getRequestPath()));
        queryWrapper.eq("http_method", request.getHttpMethod(), StringUtils.isNotBlank(request.getHttpMethod()));
        queryWrapper.eq("result", request.getResult(), StringUtils.isNotBlank(request.getResult()));
        queryWrapper.like("operation_description", request.getOperationDescription(), StringUtils.isNotBlank(request.getOperationDescription()));
        queryWrapper.eq("business_module", request.getBusinessModule(), StringUtils.isNotBlank(request.getBusinessModule()));
        queryWrapper.eq("operation_type", request.getOperationType(), StringUtils.isNotBlank(request.getOperationType()));
        queryWrapper.ge("audit_time", request.getAuditStartTime(), request.getAuditStartTime() != null);
        queryWrapper.le("audit_time", request.getAuditEndTime(), request.getAuditEndTime() != null);
        if (SqlUtils.validSortField(request.getSortField())) {
            queryWrapper.orderBy(request.getSortField(), CommonConstant.SORT_ORDER_ASC.equals(request.getSortOrder()));
        } else {
            queryWrapper.orderBy("audit_time", false).orderBy("id", false);
        }
        return queryWrapper;
    }

    @Override
    public AuditLogVO getAuditLogVO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        AuditLogVO vo = new AuditLogVO();
        BeanUtils.copyProperties(auditLog, vo);
        return vo;
    }

    @Override
    public List<AuditLogVO> getAuditLogVO(List<AuditLog> auditLogs) {
        if (CollUtil.isEmpty(auditLogs)) {
            return new ArrayList<>();
        }
        return auditLogs.stream().map(this::getAuditLogVO).collect(Collectors.toList());
    }

    @Override
    public List<AuditLogVO> listExportLogs(AuditLogExportRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        int limit = request.getExportLimit() == null ? DEFAULT_EXPORT_LIMIT : request.getExportLimit();
        if (limit <= 0 || limit > DEFAULT_EXPORT_LIMIT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "audit.export.limit.invalid");
        }
        QueryWrapper queryWrapper = getQueryWrapper(request).limit(limit);
        return getAuditLogVO(this.list(queryWrapper));
    }

    /**
     * 安全保存审计日志，失败时不影响主业务。
     */
    private void saveAuditLogSafely(AuditLogCreateRequest request) {
        auditLogExecutor.execute(() -> insertAuditLogSafely(request));
    }

    /**
     * 执行审计日志落库并吞掉审计自身异常。
     */
    private void insertAuditLogSafely(AuditLogCreateRequest request) {
        try {
            AuditLog auditLog = buildAuditLog(request);
            if (auditLogMapper != null) {
                auditLogMapper.insertSelective(auditLog);
            } else {
                this.save(auditLog);
            }
        } catch (Exception e) {
            log.error("save audit log failed", e);
        }
    }

    /**
     * 构造审计日志实体。
     */
    private AuditLog buildAuditLog(AuditLogCreateRequest request) {
        AuditLog auditLog = new AuditLog();
        BeanUtils.copyProperties(request, auditLog);
        auditLog.setRequestSummary(auditSanitizer.sanitize(request.getRequestSummary()));
        auditLog.setResponseSummary(auditSanitizer.sanitize(request.getResponseSummary()));
        auditLog.setExceptionSummary(auditSanitizer.sanitize(request.getExceptionSummary()));
        if (auditLog.getAuditTime() == null) {
            auditLog.setAuditTime(new Date());
        }
        return auditLog;
    }

    /**
     * 构造异常摘要。
     */
    private String buildExceptionSummary(Throwable throwable) {
        String message = throwable.getMessage();
        if (StringUtils.isBlank(message)) {
            return throwable.getClass().getSimpleName();
        }
        return throwable.getClass().getSimpleName() + ": " + message;
    }
}
