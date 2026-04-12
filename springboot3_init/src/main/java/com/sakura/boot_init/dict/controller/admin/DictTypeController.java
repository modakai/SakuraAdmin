package com.sakura.boot_init.dict.controller.admin;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.DeleteRequest;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.constant.UserConstant;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.dict.model.entity.DictType;
import com.sakura.boot_init.dict.service.DictTypeService;
import com.sakura.boot_init.support.annotation.AuthCheck;
import com.sakura.boot_init.dict.model.dto.DictTypeAddRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictTypeUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictTypeVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 鍚庡彴瀛楀吀绫诲瀷鎺ュ彛銆? *
 * 浣滆€咃細Sakura
 */
@RestController
@RequestMapping("/dict/type")
@Validated
public class DictTypeController {

    @Resource
    private DictTypeService dictTypeService;

    /**
     * 鏂板瀛楀吀绫诲瀷銆?     *
     * @param request 璇锋眰鍙傛暟
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏂板璁板綍 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDictType(@Valid @RequestBody DictTypeAddRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictTypeService.addDictType(request));
    }

    /**
     * 鏇存柊瀛楀吀绫诲瀷銆?     *
     * @param request 璇锋眰鍙傛暟
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏄惁鎴愬姛
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDictType(@Valid @RequestBody DictTypeUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictTypeService.updateDictType(request));
    }

    /**
     * 鍒犻櫎瀛楀吀绫诲瀷銆?     *
     * @param deleteRequest 鍒犻櫎璇锋眰
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏄惁鎴愬姛
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDictType(@Valid @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictTypeService.removeDictType(deleteRequest.getId()));
    }

    /**
     * 鏍规嵁 id 鑾峰彇瀛楀吀绫诲瀷銆?     *
     * @param id 绫诲瀷 id
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 瀛楀吀绫诲瀷璇︽儏
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<DictTypeVO> getDictTypeById(@RequestParam @Positive(message = "瀛楀吀绫诲瀷 id 蹇呴』澶т簬 0") long id,
            HttpServletRequest httpServletRequest) {
        DictType dictType = dictTypeService.getById(id);
        ThrowUtils.throwIf(dictType == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(dictTypeService.getDictTypeVO(dictType));
    }

    /**
     * 鍒嗛〉鑾峰彇瀛楀吀绫诲瀷鍒楄〃銆?     *
     * @param queryRequest 鏌ヨ璇锋眰
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鍒嗛〉缁撴灉
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DictTypeVO>> listDictTypeByPage(@Valid @RequestBody DictTypeQueryRequest queryRequest,
            HttpServletRequest httpServletRequest) {
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



