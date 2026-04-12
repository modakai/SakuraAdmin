package com.sakura.boot_init.dict.controller.admin;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.DeleteRequest;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.constant.UserConstant;
import com.sakura.boot_init.support.exception.ThrowUtils;
import com.sakura.boot_init.dict.model.entity.DictItem;
import com.sakura.boot_init.dict.service.DictItemService;
import com.sakura.boot_init.support.annotation.AuthCheck;
import com.sakura.boot_init.dict.model.dto.DictItemAddRequest;
import com.sakura.boot_init.dict.model.dto.DictItemQueryRequest;
import com.sakura.boot_init.dict.model.dto.DictItemUpdateRequest;
import com.sakura.boot_init.dict.model.vo.DictItemVO;
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
 * 鍚庡彴瀛楀吀鏄庣粏鎺ュ彛銆? *
 * 浣滆€咃細Sakura
 */
@RestController
@RequestMapping("/dict/item")
@Validated
public class DictItemController {

    @Resource
    private DictItemService dictItemService;

    /**
     * 鏂板瀛楀吀鏄庣粏銆?     *
     * @param request 璇锋眰鍙傛暟
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏂板璁板綍 id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDictItem(@Valid @RequestBody DictItemAddRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictItemService.addDictItem(request));
    }

    /**
     * 鏇存柊瀛楀吀鏄庣粏銆?     *
     * @param request 璇锋眰鍙傛暟
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏄惁鎴愬姛
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDictItem(@Valid @RequestBody DictItemUpdateRequest request,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictItemService.updateDictItem(request));
    }

    /**
     * 鍒犻櫎瀛楀吀鏄庣粏銆?     *
     * @param deleteRequest 鍒犻櫎璇锋眰
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鏄惁鎴愬姛
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteDictItem(@Valid @RequestBody DeleteRequest deleteRequest,
            HttpServletRequest httpServletRequest) {
        return ResultUtils.success(dictItemService.removeDictItem(deleteRequest.getId()));
    }

    /**
     * 鏍规嵁 id 鑾峰彇瀛楀吀鏄庣粏銆?     *
     * @param id 鏄庣粏 id
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 瀛楀吀鏄庣粏璇︽儏
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<DictItemVO> getDictItemById(@RequestParam @Positive(message = "瀛楀吀鏄庣粏 id 蹇呴』澶т簬 0") long id,
            HttpServletRequest httpServletRequest) {
        DictItem dictItem = dictItemService.getById(id);
        ThrowUtils.throwIf(dictItem == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(dictItemService.getDictItemVO(dictItem));
    }

    /**
     * 鍒嗛〉鑾峰彇瀛楀吀鏄庣粏鍒楄〃銆?     *
     * @param queryRequest 鏌ヨ璇锋眰
     * @param httpServletRequest 褰撳墠璇锋眰
     * @return 鍒嗛〉缁撴灉
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DictItemVO>> listDictItemByPage(@Valid @RequestBody DictItemQueryRequest queryRequest,
            HttpServletRequest httpServletRequest) {
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



