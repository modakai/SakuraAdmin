package com.sakura.boot_init.web.controller;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.common.BaseResponse;
import com.sakura.boot_init.common.DeleteRequest;
import com.sakura.boot_init.common.ErrorCode;
import com.sakura.boot_init.common.ResultUtils;
import com.sakura.boot_init.common.constant.UserConstant;
import com.sakura.boot_init.common.exception.BusinessException;
import com.sakura.boot_init.common.exception.ThrowUtils;
import com.sakura.boot_init.infra.persistence.entity.DictItem;
import com.sakura.boot_init.service.DictItemService;
import com.sakura.boot_init.web.annotation.AuthCheck;
import com.sakura.boot_init.web.dto.dict.DictItemAddRequest;
import com.sakura.boot_init.web.dto.dict.DictItemQueryRequest;
import com.sakura.boot_init.web.dto.dict.DictItemUpdateRequest;
import com.sakura.boot_init.web.vo.dict.DictItemVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典明细接口
 *
 * @author sakura
 */
@RestController
@RequestMapping("/dict/item")
public class DictItemController {

    @Resource
    private DictItemService dictItemService;

    /**
     * 新增字典明细
     *
     * @param request 请求参数
     * @param httpServletRequest 当前请求
     * @return 新增记录 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDictItem(@RequestBody DictItemAddRequest request, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictItemService.addDictItem(request));
    }

    /**
     * 更新字典明细
     *
     * @param request 请求参数
     * @param httpServletRequest 当前请求
     * @return 是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDictItem(@RequestBody DictItemUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictItemService.updateDictItem(request));
    }

    /**
     * 删除字典明细
     *
     * @param deleteRequest 删除请求
     * @param httpServletRequest 当前请求
     * @return 是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDictItem(@RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(dictItemService.removeDictItem(deleteRequest.getId()));
    }

    /**
     * 根据 id 获取字典明细
     *
     * @param id 明细 id
     * @param httpServletRequest 当前请求
     * @return 字典明细详情
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<DictItemVO> getDictItemById(long id, HttpServletRequest httpServletRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        DictItem dictItem = dictItemService.getById(id);
        ThrowUtils.throwIf(dictItem == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(dictItemService.getDictItemVO(dictItem));
    }

    /**
     * 分页获取字典明细列表
     *
     * @param queryRequest 查询请求
     * @param httpServletRequest 当前请求
     * @return 分页结果
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DictItemVO>> listDictItemByPage(@RequestBody DictItemQueryRequest queryRequest,
            HttpServletRequest httpServletRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();
        Page<DictItem> page = dictItemService.page(new Page<>(current, pageSize),
                dictItemService.getQueryWrapper(queryRequest));
        List<DictItemVO> dictItemVOList = dictItemService.getDictItemVO(page.getRecords());
        Page<DictItemVO> voPage = new Page<>(current, pageSize, page.getTotalRow());
        voPage.setRecords(dictItemVOList);
        return ResultUtils.success(voPage);
    }
}
