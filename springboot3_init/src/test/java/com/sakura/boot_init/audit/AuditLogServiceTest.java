package com.sakura.boot_init.audit;

import com.sakura.boot_init.audit.enums.AuditLogResultEnum;
import com.sakura.boot_init.audit.enums.AuditLogTypeEnum;
import com.sakura.boot_init.audit.model.dto.AuditLogCreateRequest;
import com.sakura.boot_init.audit.model.entity.AuditLog;
import com.sakura.boot_init.audit.repository.AuditLogMapper;
import com.sakura.boot_init.audit.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * 审计日志服务测试。
 *
 * @author Sakura
 */
class AuditLogServiceTest {

    /**
     * 登录成功事件必须保存为 LOGIN 类型并带上成功结果。
     */
    @Test
    void shouldSubmitLoginAuditLog() {
        AuditLogMapper mapper = mock(AuditLogMapper.class);
        AuditLogServiceImpl service = new AuditLogServiceImpl(mapper);
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setAccountIdentifier("admin");
        request.setUserId(1L);
        request.setIpAddress("127.0.0.1");

        service.submitLoginLog(request, true, null, 15L);

        verify(mapper, timeout(1000)).insertSelective(any(AuditLog.class));
        assertEquals(AuditLogTypeEnum.LOGIN.getValue(), request.getLogType());
        assertEquals(AuditLogResultEnum.SUCCESS.getValue(), request.getResult());
    }

    /**
     * 管理员操作失败时必须保存异常摘要和失败结果。
     */
    @Test
    void shouldSubmitFailedOperationAuditLog() {
        AuditLogMapper mapper = mock(AuditLogMapper.class);
        AuditLogServiceImpl service = new AuditLogServiceImpl(mapper);
        AuditLogCreateRequest request = new AuditLogCreateRequest();
        request.setRequestPath("/user/delete");
        request.setHttpMethod("POST");

        service.submitOperationLog(request, false, new IllegalStateException("boom"), 20L);

        verify(mapper, timeout(1000)).insertSelective(any(AuditLog.class));
        assertEquals(AuditLogTypeEnum.ADMIN_OPERATION.getValue(), request.getLogType());
        assertEquals(AuditLogResultEnum.FAILURE.getValue(), request.getResult());
        assertEquals("IllegalStateException: boom", request.getExceptionSummary());
    }
}
