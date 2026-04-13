package com.sakura.boot_init.support;

import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.PageRequest;
import com.sakura.boot_init.support.common.ResultUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 前后端响应契约测试。
 */
class ResponseContractTest {

    /**
     * 通用响应必须包含前端依赖的 success 和 extra 字段。
     */
    @Test
    void shouldExposeFrontendResponseFields() throws Exception {
        BaseResponse<String> response = ResultUtils.success("ok");

        Method getSuccess = BaseResponse.class.getMethod("isSuccess");
        Method getExtra = BaseResponse.class.getMethod("getExtra");

        assertEquals(0, response.getCode());
        assertEquals("ok", response.getMessage());
        assertTrue((Boolean) getSuccess.invoke(response));
        assertEquals(Map.of(), getExtra.invoke(response));
    }

    /**
     * 分页请求参数应与前端 page/pageSize 命名保持一致。
     */
    @Test
    void shouldUsePageAndPageSizeFields() throws Exception {
        Method getPage = PageRequest.class.getMethod("getPage");
        Method setPage = PageRequest.class.getMethod("setPage", int.class);
        Method getPageSize = PageRequest.class.getMethod("getPageSize");
        Method setPageSize = PageRequest.class.getMethod("setPageSize", int.class);

        PageRequest pageRequest = new PageRequest();
        setPage.invoke(pageRequest, 3);
        setPageSize.invoke(pageRequest, 20);

        assertEquals(3, getPage.invoke(pageRequest));
        assertEquals(20, getPageSize.invoke(pageRequest));
    }
}
