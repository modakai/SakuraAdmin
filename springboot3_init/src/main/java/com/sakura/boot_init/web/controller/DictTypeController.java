package com.sakura.boot_init.web.controller;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.common.BaseResponse;
import com.sakura.boot_init.common.DeleteRequest;
import com.sakura.boot_init.common.ErrorCode;
import com.sakura.boot_init.common.ResultUtils;
import com.sakura.boot_init.common.constant.UserConstant;
import com.sakura.boot_init.common.exception.BusinessException;
import com.sakura.boot_init.common.exception.ThrowUtils;
import com.sakura.boot_init.infra.persistence.entity.DictType;
import com.sakura.boot_init.service.DictTypeService;
import com.sakura.boot_init.web.annotation.AuthCheck;
import com.sakura.boot_init.web.dto.dict.DictTypeAddRequest;
import com.sakura.boot_init.web.dto.dict.DictTypeQueryRequest;
import com.sakura.boot_init.web.dto.dict.DictTypeUpdateRequest;
import com.sakura.boot_init.web.vo.dict.DictTypeVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典类型接口
 *
 * @author sakura
 */
@RestController
@RequestMapping("/dict/type")
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    /**
     * 新增字典类型
     *
     * @param request 请求参数
     * @param httpServletRequest 当前请求
     * @return 新增记录 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDictType(@RequestBody DictTypeAddRequest request, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictTypeService.addDictType(request));
    }

    /**
     * 更新字典类型
     *
     * @param request 请求参数
     * @param httpServletRequest 当前请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDictType(@RequestBody DictTypeUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictTypeService.updateDictType(request));
    }

    /**
     * 删除字典类型
     *
     * @param deleteRequest 删除请求
     * @param httpServletRequest 当前请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDictType(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(dictTypeService.removeDictType(deleteRequest.getId()));
    }

    /**
     * 根据 id 获取字典类型
     *
     * @param id 类型 id
     * @param httpServletRequest 当前请求
     * @return 字典类型详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<DictTypeVO> getDictTypeById(long id, HttpServletRequest httpServletRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictType dictType = dictTypeService.getById(id);
        ThrowUtils.throwIf(dictType == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(dictTypeService.getDictTypeVO(dictType));
    }

    /**
     * 分页获取字典类型列表
     *
     * @param queryRequest 查询请求
     * @param httpServletRequest 当前请求
     * @return 分页结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DictTypeVO>> listDictTypeByPage(@RequestBody DictTypeQueryRequest queryRequest,
            HttpServletRequest httpServletRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        Page<DictType> page = dictTypeService.page(new Page<>(current, pageSize),
                dictTypeService.getQueryWrapper(queryRequest));
        List<DictTypeVO> dictTypeVOList = dictTypeService.getDictTypeVO(page.getRecords());
        Page<DictTypeVO> voPage = new Page<>(current, pageSize, page.getTotalRow());
        voPage.setRecords(dictTypeVOList);
        return ResultUtils.success(voPage);
    }
}
