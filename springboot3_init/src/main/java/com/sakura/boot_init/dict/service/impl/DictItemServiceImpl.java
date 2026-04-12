package com.sakura.boot_init.dict.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.constant.CommonConstant;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.support.util.SqlUtils;
import com.sakura.boot_init.dict.repository.DictTypeMapper;
import com.sakura.boot_init.dict.repository.DictItemMapper;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.model.dto.DictItemAddRequest;
import com.sakura.boot_init.dict.model.dto.DictItemQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictItemUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictItemVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 瀛楀吀鏄庣粏鏈嶅姟瀹炵幇
 *
 * @author sakura
 */
@Service
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemService {

    @Resource
    private DictTypeMapper dictTypeMapper;

    @Override
    public Long addDictItem(DictItemAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictItem dictItem = new DictItem();
        BeanUtils.copyProperties(request, dictItem);
        validDictItem(dictItem, true);
        boolean result = this.save(dictItem);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return dictItem.getId();
    }

    @Override
    public boolean updateDictItem(DictItemUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictItem oldDictItem = this.getById(request.getId());
        ThrowUtils.throwIf(oldDictItem == null, ErrorCode.NOT_FOUND_ERROR);
        DictItem dictItem = new DictItem();
        BeanUtils.copyProperties(request, dictItem);
        validDictItem(dictItem, false);
        return this.updateById(dictItem);
    }

    @Override
    public boolean removeDictItem(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictItem oldDictItem = this.getById(id);
        ThrowUtils.throwIf(oldDictItem == null, ErrorCode.NOT_FOUND_ERROR);
        return this.removeById(id);
    }

    @Override
    public QueryWrapper getQueryWrapper(DictItemQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "璇锋眰鍙傛暟涓虹┖");
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", queryRequest.getId(), queryRequest.getId() != null);
        queryWrapper.eq("dict_type_id", queryRequest.getDictTypeId(), queryRequest.getDictTypeId() != null);
        queryWrapper.like("dict_label", queryRequest.getDictLabel(), StringUtils.isNotBlank(queryRequest.getDictLabel()));
        queryWrapper.like("dict_value", queryRequest.getDictValue(), StringUtils.isNotBlank(queryRequest.getDictValue()));
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
    public long countEnabledByTypeId(Long dictTypeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("dict_type_id", dictTypeId)
                .eq("is_delete", 0);
        return this.count(queryWrapper);
    }

    @Override
    public List<DictItem> listEnabledByTypeId(Long dictTypeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("dict_type_id", dictTypeId)
                .eq("status", 1)
                .eq("is_delete", 0)
                .orderBy("sort_order", true)
                .orderBy("id", false);
        return this.list(queryWrapper);
    }

    @Override
    public boolean existsByTypeAndValue(Long dictTypeId, String dictValue, Long excludeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("dict_type_id", dictTypeId)
                .eq("dict_value", dictValue);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public DictItemVO getDictItemVO(DictItem dictItem) {
        if (dictItem == null) {
            return null;
        }
        DictItemVO dictItemVO = new DictItemVO();
        BeanUtils.copyProperties(dictItem, dictItemVO);
        return dictItemVO;
    }

    @Override
    public List<DictItemVO> getDictItemVO(List<DictItem> dictItemList) {
        if (CollUtil.isEmpty(dictItemList)) {
            return new ArrayList<>();
        }
        return dictItemList.stream().map(this::getDictItemVO).collect(Collectors.toList());
    }

    /**
     * 鏍￠獙瀛楀吀鏄庣粏鍙傛暟
     *
     * @param dictItem 瀛楀吀鏄庣粏
     * @param add 鏄惁鏂板
     */
    private void validDictItem(DictItem dictItem, boolean add) {
        if (dictItem == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!add && (dictItem.getId() == null || dictItem.getId() <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "瀛楀吀鏄庣粏 id 闈炴硶");
        }
        if (dictItem.getDictTypeId() == null || dictItem.getDictTypeId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字典类型不存在");
        }
        DictType dictType = dictTypeMapper.selectOneById(dictItem.getDictTypeId());
        ThrowUtils.throwIf(dictType == null, ErrorCode.NOT_FOUND_ERROR, "字典类型不存在");
        if (StringUtils.isBlank(dictItem.getDictLabel())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "瀛楀吀鏍囩涓嶈兘涓虹┖");
        }
        if (StringUtils.isBlank(dictItem.getDictValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字典值不能为空");
        }
        if (dictItem.getStatus() == null || (dictItem.getStatus() != 0 && dictItem.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字典状态非法");
        }
        if (dictItem.getSortOrder() == null) {
            dictItem.setSortOrder(0);
        }
        boolean exists = existsByTypeAndValue(dictItem.getDictTypeId(), dictItem.getDictValue(), add ? null : dictItem.getId());
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "鍚屼竴瀛楀吀绫诲瀷涓嬬殑瀛楀吀鍊煎凡瀛樺湪");
    }
}



