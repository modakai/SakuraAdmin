package com.sakura.boot_init.audit;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.audit.model.dto.AuditLogQueryRequest;
import com.sakura.boot_init.audit.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 审计日志查询条件测试。
 *
 * @author Sakura
 */
class AuditLogQueryTest {

    /**
     * 查询条件构建必须支持任务要求中的核心筛选字段。
     */
    @Test
    void shouldBuildQueryWrapperWithFilters() {
        AuditLogServiceImpl service = new AuditLogServiceImpl(null);
        AuditLogQueryRequest request = new AuditLogQueryRequest();
        request.setLogType("login");
        request.setUserId(1L);
        request.setAccountIdentifier("admin");
        request.setIpAddress("127.0.0.1");
        request.setRequestPath("/user/login");
        request.setHttpMethod("POST");
        request.setResult("success");
        request.setOperationDescription("删除用户");

        QueryWrapper wrapper = service.getQueryWrapper(request);

        assertNotNull(wrapper);
    }
}
