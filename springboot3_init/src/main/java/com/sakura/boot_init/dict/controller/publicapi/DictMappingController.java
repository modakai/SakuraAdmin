package com.sakura.boot_init.dict.controller.publicapi;

import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.dict.service.DictMappingService;
import com.sakura.boot_init.dict.model.dto.DictBatchQueryRequest;
import com.sakura.boot_init.dict.model.vo.DictItemSimpleVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 瀛楀吀鏄犲皠鎺ュ彛
 *
 * @author sakura
 */
@RestController
@RequestMapping("/dict")
@Validated
public class DictMappingController {

    @Resource
    private DictMappingService dictMappingService;

    /**
     * 鑾峰彇鍗曚釜瀛楀吀鏄犲皠
     *
     * @param dictCode 瀛楀吀缂栫爜
     * @return 瀛楀吀鏄犲皠鍒楄〃
     */
    @GetMapping("/map")
    public BaseResponse<List<DictItemSimpleVO>> getDictMap(@RequestParam @NotBlank(message = "瀛楀吀缂栫爜涓嶈兘涓虹┖") String dictCode) {
        return ResultUtils.success(dictMappingService.getEnabledItemsByCode(dictCode));
    }

    /**
     * 鎵归噺鑾峰彇瀛楀吀鏄犲皠
     *
     * @param request 鎵归噺鏌ヨ璇锋眰
     * @return 瀛楀吀鏄犲皠缁撴灉
     */
    @PostMapping("/map/batch")
    public BaseResponse<Map<String, List<DictItemSimpleVO>>> getDictMapBatch(@Valid @RequestBody DictBatchQueryRequest request) {
        return ResultUtils.success(dictMappingService.getEnabledItemMap(request.getDictCodes()));
    }

    /**
     * 鏍规嵁缂栫爜鍜屽€艰幏鍙栨爣绛?     *
     * @param dictCode 瀛楀吀缂栫爜
     * @param value 瀛楀吀鍊?     * @return 鏍囩鏂囨湰
     */
    @GetMapping("/label")
    public BaseResponse<String> getLabelByCodeAndValue(
            @RequestParam @NotBlank(message = "瀛楀吀缂栫爜涓嶈兘涓虹┖") String dictCode,
            @RequestParam @NotBlank(message = "字典值不能为空") String value) {
        return ResultUtils.success(dictMappingService.getLabelByCodeAndValue(dictCode, value));
    }
}



