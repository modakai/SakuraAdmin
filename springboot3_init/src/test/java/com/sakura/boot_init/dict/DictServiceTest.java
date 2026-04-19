package com.sakura.boot_init.dict;

import com.sakura.boot_init.shared.exception.BusinessException;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.dict.service.impl.DictMappingServiceImpl;
import com.sakura.boot_init.dict.service.impl.DictTypeServiceImpl;
import com.sakura.boot_init.dict.model.dto.DictTypeAddRequest;
import com.sakura.boot_init.dict.model.vo.DictItemSimpleVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 字典模块服务测试
 *
 * @author sakura
 */
class DictServiceTest {

    /**
     * 新增字典类型时，重复编码必须拦截。
     */
    @Test
    void shouldRejectDuplicateDictCodeWhenAddingType() throws Exception {
        DictTypeServiceImpl dictTypeService = spy(new DictTypeServiceImpl());
        doReturn(true).when(dictTypeService).existsByDictCode("user_status", null);

        DictTypeAddRequest request = new DictTypeAddRequest();
        request.setDictCode("user_status");
        request.setDictName("用户状态");
        request.setStatus(1);

        assertThrows(BusinessException.class, () -> dictTypeService.addDictType(request));
    }

    /**
     * 删除字典类型前，如果仍存在明细数据必须拦截。
     */
    @Test
    void shouldRejectDeleteTypeWhenItemsStillExist() throws Exception {
        DictTypeServiceImpl dictTypeService = spy(new DictTypeServiceImpl());
        DictItemService dictItemService = mock(DictItemService.class);
        injectField(dictTypeService, "dictItemService", dictItemService);
        doReturn(2L).when(dictItemService).countEnabledByTypeId(1001L);

        assertThrows(BusinessException.class, () -> dictTypeService.removeDictType(1001L));
    }

    /**
     * 公共映射只返回启用项，并按排序号升序输出。
     */
    @Test
    void shouldReturnEnabledItemsSortedForMapping() throws Exception {
        DictMappingServiceImpl mappingService = new DictMappingServiceImpl();
        DictTypeService dictTypeService = mock(DictTypeService.class);
        DictItemService dictItemService = mock(DictItemService.class);
        injectField(mappingService, "dictTypeService", dictTypeService);
        injectField(mappingService, "dictItemService", dictItemService);

        DictType dictType = new DictType();
        dictType.setId(10L);
        dictType.setDictCode("user_status");
        dictType.setStatus(1);
        when(dictTypeService.getByDictCode("user_status")).thenReturn(dictType);

        DictItem second = new DictItem();
        second.setDictLabel("启用");
        second.setDictValue("1");
        second.setSortOrder(2);
        second.setStatus(1);
        DictItem first = new DictItem();
        first.setDictLabel("禁用");
        first.setDictValue("0");
        first.setSortOrder(1);
        first.setStatus(1);
        when(dictItemService.listEnabledByTypeId(eq(10L))).thenReturn(List.of(second, first));

        List<DictItemSimpleVO> result = mappingService.getEnabledItemsByCode("user_status");

        assertEquals(2, result.size());
        assertEquals("0", result.get(0).getValue());
        assertEquals("1", result.get(1).getValue());
        verify(dictItemService).listEnabledByTypeId(anyLong());
    }

    /**
     * 反射注入测试依赖，避免引入 Spring 上下文。
     */
    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

