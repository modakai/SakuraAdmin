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
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.dict.model.dto.DictTypeAddRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictTypeVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 瀛楀吀绫诲瀷鏈嶅姟瀹炵幇
 *
 * @author sakura
 */
@Service
public class DictTypeServiceImpl extends ServiceImpl<DictTypeMapper, DictType> implements DictTypeService {

    /**
     * 瀛楀吀缂栫爜鍚堟硶鏍煎紡
     */
    private static final Pattern DICT_CODE_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*$");

    @Resource
    private DictItemService dictItemService;

    @Override
    public Long addDictType(DictTypeAddRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictType dictType = new DictType();
        BeanUtils.copyProperties(request, dictType);
        validDictType(dictType, true);
        boolean result = this.save(dictType);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return dictType.getId();
    }

    @Override
    public boolean updateDictType(DictTypeUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictType oldDictType = this.getById(request.getId());
        ThrowUtils.throwIf(oldDictType == null, ErrorCode.NOT_FOUND_ERROR);
        DictType dictType = new DictType();
        BeanUtils.copyProperties(request, dictType);
        validDictType(dictType, false);
        return this.updateById(dictType);
    }

    @Override
    public boolean removeDictType(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long itemCount = dictItemService.countEnabledByTypeId(id);
        ThrowUtils.throwIf(itemCount > 0, ErrorCode.OPERATION_ERROR, "请先删除该字典类型下的明细数据");
        DictType oldDictType = this.getById(id);
        ThrowUtils.throwIf(oldDictType == null, ErrorCode.NOT_FOUND_ERROR);
        return this.removeById(id);
    }

    @Override
    public QueryWrapper getQueryWrapper(DictTypeQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "璇锋眰鍙傛暟涓虹┖");
        }
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq("id", queryRequest.getId(), queryRequest.getId() != null);
        queryWrapper.like("dict_code", queryRequest.getDictCode(), StringUtils.isNotBlank(queryRequest.getDictCode()));
        queryWrapper.like("dict_name", queryRequest.getDictName(), StringUtils.isNotBlank(queryRequest.getDictName()));
        queryWrapper.eq("status", queryRequest.getStatus(), queryRequest.getStatus() != null);
        if (SqlUtils.validSortField(queryRequest.getSortField())) {
            queryWrapper.orderBy(queryRequest.getSortField(),
                    CommonConstant.SORT_ORDER_ASC.equals(queryRequest.getSortOrder()));
        } else {
            queryWrapper.orderBy("id", false);
        }
        return queryWrapper;
    }

    @Override
    public boolean existsByDictCode(String dictCode, Long excludeId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("dict_code", dictCode);
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public DictType getByDictCode(String dictCode) {
        if (StringUtils.isBlank(dictCode)) {
            return null;
        }
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("dict_code", dictCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public DictTypeVO getDictTypeVO(DictType dictType) {
        if (dictType == null) {
            return null;
        }
        DictTypeVO dictTypeVO = new DictTypeVO();
        BeanUtils.copyProperties(dictType, dictTypeVO);
        return dictTypeVO;
    }

    @Override
    public List<DictTypeVO> getDictTypeVO(List<DictType> dictTypeList) {
        if (CollUtil.isEmpty(dictTypeList)) {
            return new ArrayList<>();
        }
        return dictTypeList.stream().map(this::getDictTypeVO).collect(Collectors.toList());
    }

    /**
     * 鏍￠獙瀛楀吀绫诲瀷鍙傛暟
     *
     * @param dictType 瀛楀吀绫诲瀷瀹炰綋
     * @param add 鏄惁鏂板
     */
    private void validDictType(DictType dictType, boolean add) {
        if (dictType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (!add && (dictType.getId() == null || dictType.getId() <= 0)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "瀛楀吀绫诲瀷 id 闈炴硶");
        }
        if (StringUtils.isBlank(dictType.getDictCode()) || !DICT_CODE_PATTERN.matcher(dictType.getDictCode()).matches()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字典编码格式错误，仅支持字母、数字和下划线，且必须以字母开头");
        }
        if (StringUtils.isBlank(dictType.getDictName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "瀛楀吀鍚嶇О涓嶈兘涓虹┖");
        }
        if (dictType.getStatus() == null || (dictType.getStatus() != 0 && dictType.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "字典状态非法");
        }
        boolean exists = existsByDictCode(dictType.getDictCode(), add ? null : dictType.getId());
        ThrowUtils.throwIf(exists, ErrorCode.PARAMS_ERROR, "字典编码已存在");
    }
}



