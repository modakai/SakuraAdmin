package com.sakura.boot_init.audit.controller;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.shared.enums.AuditOperationTypeEnum;
import com.sakura.boot_init.audit.model.dto.AuditLogExportRequest;
import com.sakura.boot_init.audit.model.dto.AuditLogQueryRequest;
import com.sakura.boot_init.audit.model.entity.AuditLog;
import com.sakura.boot_init.audit.model.vo.AuditLogVO;
import com.sakura.boot_init.audit.service.AuditLogService;
import com.sakura.boot_init.shared.annotation.AuditLogRecord;
import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.constant.UserConstant;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台审计日志接口。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/audit/log")
@Validated
public class AuditLogController {

    /**
     * 审计日志服务。
     */
    @Resource
    private AuditLogService auditLogService;

    /**
     * 分页查询审计日志。
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AuditLogVO>> listAuditLogByPage(@Valid @RequestBody AuditLogQueryRequest request,
            HttpServletRequest httpServletRequest) {
        long current = request.getPage();
        long pageSize = request.getPageSize();
        Page<AuditLog> page = auditLogService.page(new Page<>(current, pageSize), auditLogService.getQueryWrapper(request));
        List<AuditLogVO> voList = auditLogService.getAuditLogVO(page.getRecords());
        Page<AuditLogVO> voPage = new Page<>(current, pageSize, page.getTotalRow());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }

    /**
     * 获取审计日志详情。
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AuditLogVO> getAuditLog(@RequestParam @Positive(message = "审计日志 id 必须大于 0") long id,
            HttpServletRequest httpServletRequest) {
        AuditLog auditLog = auditLogService.getById(id);
        ThrowUtils.throwIf(auditLog == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(auditLogService.getAuditLogVO(auditLog));
    }

    /**
     * 导出审计日志 CSV。
     */
    @PostMapping("/export")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @AuditLogRecord(description = "导出审计日志", module = "审计日志", operationType = AuditOperationTypeEnum.EXPORT)
    public ResponseEntity<byte[]> exportAuditLog(@Valid @RequestBody AuditLogExportRequest request,
            HttpServletRequest httpServletRequest) {
        List<AuditLogVO> logs = auditLogService.listExportLogs(request);
        byte[] body = buildCsv(logs).getBytes(StandardCharsets.UTF_8);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename("audit-logs.csv", StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(body);
    }

    /**
     * 构造 CSV 内容。
     */
    private String buildCsv(List<AuditLogVO> logs) {
        String header = "id,logType,userId,accountIdentifier,ipAddress,requestPath,httpMethod,operationDescription,result,statusCode,costMillis,auditTime";
        String rows = logs.stream()
                .map(log -> String.join(",",
                        csv(log.getId()),
                        csv(log.getLogType()),
                        csv(log.getUserId()),
                        csv(log.getAccountIdentifier()),
                        csv(log.getIpAddress()),
                        csv(log.getRequestPath()),
                        csv(log.getHttpMethod()),
                        csv(log.getOperationDescription()),
                        csv(log.getResult()),
                        csv(log.getStatusCode()),
                        csv(log.getCostMillis()),
                        csv(log.getAuditTime())))
                .collect(Collectors.joining("\n"));
        return rows.isBlank() ? header + "\n" : header + "\n" + rows + "\n";
    }

    /**
     * 转义 CSV 字段。
     */
    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        return "\"" + String.valueOf(value).replace("\"", "\"\"") + "\"";
    }
}
