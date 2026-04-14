package com.sakura.boot_init.agreement.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.agreement.model.dto.AgreementAddRequest;
import com.sakura.boot_init.agreement.model.dto.AgreementQueryRequest;
import com.sakura.boot_init.agreement.model.dto.AgreementUpdateRequest;
import com.sakura.boot_init.agreement.model.entity.Agreement;
import com.sakura.boot_init.agreement.model.vo.AgreementVO;
import com.sakura.boot_init.agreement.repository.AgreementMapper;
import com.sakura.boot_init.agreement.service.AgreementService;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.support.util.SqlUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议内容服务实现。
 *
 * @author Sakura
 */
@Service
public class AgreementServiceImpl extends ServiceImpl<AgreementMapper, Agreement> implements AgreementService {

    /**
     * 协议类型字典编码。
     */
    private static final String AGREEMENT_TYPE_DICT_CODE = "agreement_type";

    @Resource
    private DictTypeService dictTypeService;

    @Resource
    private DictItemService dictItemService;

    @Override
    public Long addAgreement(AgreementAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Agreement agreement = new Agreement();
        BeanUtils.copyProperties(request, agreement);
        validAgreement(agreement, true);
        boolean result = this.save(agreement);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return agreement.getId();
    }

    @Override
    public boolean updateAgreement(AgreementUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        assertAgreementExists(request.getId());
        Agreement agreement = new Agreement();
        BeanUtils.copyProperties(request, agreement);
        validAgreement(agreement, false);
        return this.updateById(agreement);
    }

    @Override
    public boolean removeAgreement(Long id) {
        assertAgreementExists(id);
        return this.removeById(id);
    }

    @Override
    public QueryWrapper getQueryWrapper(AgreementQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.param.null");
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", queryRequest.getId(), queryRequest.getId() != null);
        queryWrapper.eq("agreement_type", queryRequest.getAgreementType(), StringUtils.isNotBlank(queryRequest.getAgreementType()));
        queryWrapper.like("title", queryRequest.getTitle(), StringUtils.isNotBlank(queryRequest.getTitle()));
        queryWrapper.eq("status", queryRequest.getStatus(), queryRequest.getStatus() != null);
        queryWrapper.orderBy("sort_order", true);
        queryWrapper.orderBy("id", false);
        if (SqlUtils.validSortField(queryRequest.getSortField())) {
            queryWrapper.orderBy(queryRequest.getSortField(),
                    CommonConstant.SORT_ORDER_ASC.equals(queryRequest.getSortOrder()));
        }
        return queryWrapper;
    }

    @Override
    public boolean existsByAgreementType(String agreementType, Long excludeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agreement_type", agreementType);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public Agreement getByAgreementType(String agreementType) {
        if (StringUtils.isBlank(agreementType)) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("agreement_type", agreementType);
        return this.getOne(queryWrapper);
    }

    @Override
    public Agreement getEnabledAgreementByType(String agreementType) {
        Agreement agreement = getByAgreementType(agreementType);
        ThrowUtils.throwIf(agreement == null, ErrorCode.NOT_FOUND_ERROR, "agreement.not_found");
        ThrowUtils.throwIf(agreement.getStatus() == null || agreement.getStatus() != 1,
                ErrorCode.NOT_FOUND_ERROR, "agreement.not_found");
        return agreement;
    }

    @Override
    public AgreementVO getAgreementVO(Agreement agreement) {
        if (agreement == null) {
            return null;
        }
        AgreementVO agreementVO = new AgreementVO();
        BeanUtils.copyProperties(agreement, agreementVO);
        return agreementVO;
    }

    @Override
    public List<AgreementVO> getAgreementVO(List<Agreement> agreementList) {
        if (CollUtil.isEmpty(agreementList)) {
            return new ArrayList<>();
        }
        return agreementList.stream().map(this::getAgreementVO).collect(Collectors.toList());
    }

    /**
     * 断言协议记录存在。
     *
     * @param id 协议 id
     */
    public void assertAgreementExists(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Agreement oldAgreement = this.getById(id);
        ThrowUtils.throwIf(oldAgreement == null, ErrorCode.NOT_FOUND_ERROR);
    }

    /**
     * 校验协议内容参数。
     *
     * @param agreement 协议实体
     * @param add 是否新增
     */
    private void validAgreement(Agreement agreement, boolean add) {
        if (agreement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!add && (agreement.getId() == null || agreement.getId() <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.id.invalid");
        }
        if (StringUtils.isBlank(agreement.getAgreementType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.type.blank");
        }
        if (StringUtils.isBlank(agreement.getTitle())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.title.blank");
        }
        if (StringUtils.isBlank(agreement.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.content.blank");
        }
        if (agreement.getStatus() == null || (agreement.getStatus() != 0 && agreement.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "agreement.status.invalid");
        }
        if (agreement.getSortOrder() == null) {
            agreement.setSortOrder(0);
        }
        validateAgreementType(agreement.getAgreementType());
        boolean exists = existsByAgreementType(agreement.getAgreementType(), add ? null : agreement.getId());
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "agreement.exists");
    }

    /**
     * 校验协议类型是否存在于字典配置中。
     *
     * @param agreementType 协议类型编码
     */
    private void validateAgreementType(String agreementType) {
        DictType dictType = dictTypeService.getByDictCode(AGREEMENT_TYPE_DICT_CODE);
        ThrowUtils.throwIf(dictType == null, ErrorCode.NOT_FOUND_ERROR, "agreement.type.dict_not_found");
        List<DictItem> dictItemList = dictItemService.listEnabledByTypeId(dictType.getId());
        boolean exists = dictItemList.stream()
                .anyMatch(item -> StringUtils.equals(item.getDictValue(), agreementType));
        ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "agreement.type.not_found");
    }
}
