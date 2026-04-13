package com.sakura.boot_init.agreement;

import com.mybatisflex.core.query.QueryWrapper;
import com.sakura.boot_init.agreement.model.dto.AgreementAddRequest;
import com.sakura.boot_init.agreement.model.dto.AgreementQueryRequest;
import com.sakura.boot_init.agreement.model.entity.Agreement;
import com.sakura.boot_init.agreement.service.impl.AgreementServiceImpl;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.support.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 协议模块服务测试。
 *
 * @author Sakura
 */
class AgreementServiceTest {

    /**
     * 新增协议时，同类型重复数据必须拦截。
     */
    @Test
    void shouldRejectDuplicateAgreementTypeWhenAdding() throws Exception {
        AgreementServiceImpl agreementService = spy(new AgreementServiceImpl());
        DictTypeService dictTypeService = mock(DictTypeService.class);
        DictItemService dictItemService = mock(DictItemService.class);
        injectField(agreementService, "dictTypeService", dictTypeService);
        injectField(agreementService, "dictItemService", dictItemService);

        DictType dictType = new DictType();
        dictType.setId(1L);
        dictType.setDictCode("agreement_type");
        dictType.setStatus(1);
        DictItem dictItem = new DictItem();
        dictItem.setDictValue("user_agreement");
        dictItem.setStatus(1);
        when(dictTypeService.getByDictCode("agreement_type")).thenReturn(dictType);
        when(dictItemService.listEnabledByTypeId(1L)).thenReturn(java.util.List.of(dictItem));
        doReturn(true).when(agreementService).existsByAgreementType("user_agreement", null);

        AgreementAddRequest request = new AgreementAddRequest();
        request.setAgreementType("user_agreement");
        request.setTitle("用户协议");
        request.setContent("<p>内容</p>");
        request.setStatus(1);

        assertThrows(BusinessException.class, () -> agreementService.addAgreement(request));
    }

    /**
     * 更新协议前，不存在的记录必须拦截。
     */
    @Test
    void shouldRejectUpdateWhenAgreementDoesNotExist() {
        AgreementServiceImpl agreementService = spy(new AgreementServiceImpl());
        doReturn(null).when(agreementService).getById(1001L);

        assertThrows(BusinessException.class, () -> agreementService.assertAgreementExists(1001L));
    }

    /**
     * 查询构造器应包含标题、类型、状态和默认排序。
     */
    @Test
    void shouldBuildQueryWrapperWithDefaultSort() {
        AgreementServiceImpl agreementService = new AgreementServiceImpl();
        AgreementQueryRequest request = new AgreementQueryRequest();
        request.setAgreementType("privacy_policy");
        request.setTitle("隐私");
        request.setStatus(1);

        QueryWrapper queryWrapper = agreementService.getQueryWrapper(request);
        String sql = queryWrapper.toSQL();

        assertEquals(true, sql.contains("agreement_type"));
        assertEquals(true, sql.contains("title"));
        assertEquals(true, sql.contains("status"));
        assertEquals(true, sql.contains("sort_order"));
    }

    /**
     * 公共查询只返回启用协议。
     */
    @Test
    void shouldOnlyReturnEnabledAgreementForPublicQuery() throws Exception {
        AgreementServiceImpl agreementService = spy(new AgreementServiceImpl());
        Agreement disabledAgreement = new Agreement();
        disabledAgreement.setAgreementType("privacy_policy");
        disabledAgreement.setStatus(0);
        doReturn(disabledAgreement).when(agreementService).getByAgreementType("privacy_policy");

        assertThrows(BusinessException.class, () -> agreementService.getEnabledAgreementByType("privacy_policy"));
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
