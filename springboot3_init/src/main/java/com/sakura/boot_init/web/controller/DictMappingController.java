package com.sakura.boot_init.web.controller;

import com.sakura.boot_init.common.BaseResponse;
import com.sakura.boot_init.common.ErrorCode;
import com.sakura.boot_init.common.ResultUtils;
import com.sakura.boot_init.common.exception.BusinessException;
import com.sakura.boot_init.service.DictMappingService;
import com.sakura.boot_init.web.dto.dict.DictBatchQueryRequest;
import com.sakura.boot_init.web.vo.dict.DictItemSimpleVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 字典映射接口
 *
 * @author sakura
 */
@RestController
@RequestMapping("/dict")
public class DictMappingController {

    @Resource
    private DictMappingService dictMappingService;

    /**
     * 获取单个字典映射
     *
     * @param dictCode 字典编码
     * @return 字典映射列表
     */
    @GetMapping("/map")
    public BaseResponse<List<DictItemSimpleVO>> getDictMap(String dictCode) {
        if (dictCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(dictMappingService.getEnabledItemsByCode(dictCode));
    }

    /**
     * 批量获取字典映射
     *
     * @param request 批量查询请求
     * @return 字典映射结果
     */
    @PostMapping("/map/batch")
    public BaseResponse<Map<String, List<DictItemSimpleVO>>> getDictMapBatch(@RequestBody DictBatchQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(dictMappingService.getEnabledItemMap(request.getDictCodes()));
    }

    /**
     * 根据编码和值获取标签
     *
     * @param dictCode 字典编码
     * @param value 字典值
     * @return 标签文本
     */
    @GetMapping("/label")
    public BaseResponse<String> getLabelByCodeAndValue(String dictCode, String value) {
        if (dictCode == null || value == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(dictMappingService.getLabelByCodeAndValue(dictCode, value));
    }
}
