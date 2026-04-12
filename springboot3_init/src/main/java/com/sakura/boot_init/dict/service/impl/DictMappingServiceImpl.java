package com.sakura.boot_init.dict.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.dict.service.DictMappingService;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.dict.model.vo.DictItemSimpleVO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 瀛楀吀鏄犲皠鏈嶅姟瀹炵幇
 *
 * @author sakura
 */
@Service
public class DictMappingServiceImpl implements DictMappingService {

    @Resource
    private DictTypeService dictTypeService;

    @Resource
    private DictItemService dictItemService;

    @Override
    public List<DictItemSimpleVO> getEnabledItemsByCode(String dictCode) {
        if (StringUtils.isBlank(dictCode)) {
            return new ArrayList<>();
        }
        DictType dictType = dictTypeService.getByDictCode(dictCode);
        if (dictType == null || dictType.getStatus() == null || dictType.getStatus() != 1) {
            return new ArrayList<>();
        }
        return dictItemService.listEnabledByTypeId(dictType.getId()).stream()
                .sorted(Comparator.comparing(item -> item.getSortOrder() == null ? 0 : item.getSortOrder()))
                .map(this::toSimpleVO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<DictItemSimpleVO>> getEnabledItemMap(List<String> dictCodes) {
        Map<String, List<DictItemSimpleVO>> resultMap = new LinkedHashMap<>();
        if (CollUtil.isEmpty(dictCodes)) {
            return resultMap;
        }
        for (String dictCode : dictCodes) {
            resultMap.put(dictCode, getEnabledItemsByCode(dictCode));
        }
        return resultMap;
    }

    @Override
    public String getLabelByCodeAndValue(String dictCode, String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        for (DictItemSimpleVO item : getEnabledItemsByCode(dictCode)) {
            if (value.equals(item.getValue())) {
                return item.getLabel();
            }
        }
        return "";
    }

    /**
     * 杞崲涓烘渶灏忓瓧鍏告槧灏勫璞?     *
     * @param dictItem 瀛楀吀鏄庣粏
     * @return 鏄犲皠瀵硅薄
     */
    private DictItemSimpleVO toSimpleVO(DictItem dictItem) {
        DictItemSimpleVO simpleVO = new DictItemSimpleVO();
        simpleVO.setLabel(dictItem.getDictLabel());
        simpleVO.setValue(dictItem.getDictValue());
        simpleVO.setTagType(dictItem.getTagType());
        simpleVO.setSortOrder(dictItem.getSortOrder());
        simpleVO.setExtJson(dictItem.getExtJson());
        return simpleVO;
    }
}



