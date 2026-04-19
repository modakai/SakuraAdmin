package com.sakura.boot_init.shared;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sakura.boot_init.infrastructure.config.JsonConfig;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.PageRequest;
import com.sakura.boot_init.shared.common.ResultUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

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

    /**
     * Long 类型 id 必须序列化为字符串，避免前端 JSON.parse 后出现精度丢失。
     */
    @Test
    void shouldSerializeLongResponseDataAsString() throws Exception {
        ObjectMapper objectMapper = new JsonConfig().jacksonObjectMapper(new Jackson2ObjectMapperBuilder());
        BaseResponse<Long> response = ResultUtils.success(95939540536000160L);

        JsonNode jsonNode = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertTrue(jsonNode.get("data").isTextual());
        assertEquals("95939540536000160", jsonNode.get("data").asText());
    }
}
