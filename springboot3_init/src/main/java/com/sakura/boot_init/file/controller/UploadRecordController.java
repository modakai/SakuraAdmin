package com.sakura.boot_init.file.controller;

import com.mybatisflex.core.paginate.Page;
import com.sakura.boot_init.file.model.dto.UploadRecordQueryRequest;
import com.sakura.boot_init.file.model.entity.UploadRecord;
import com.sakura.boot_init.file.model.vo.UploadRecordVO;
import com.sakura.boot_init.file.service.UploadRecordService;
import com.sakura.boot_init.shared.annotation.AuthCheck;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ResultUtils;
import com.sakura.boot_init.shared.constant.UserConstant;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台上传记录接口。
 *
 * @author Sakura
 */
@RestController
@RequestMapping("/admin/upload-record")
public class UploadRecordController {

    /**
     * 上传记录服务。
     */
    @Resource
    private UploadRecordService uploadRecordService;

    /**
     * 分页查询上传成功记录。
     *
     * @param request 查询请求
     * @return 上传记录分页
     */
    @PostMapping("/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UploadRecordVO>> listUploadRecordByPage(
            @Valid @RequestBody UploadRecordQueryRequest request) {
        long current = request.getPage();
        long pageSize = request.getPageSize();
        Page<UploadRecord> page = uploadRecordService.page(new Page<>(current, pageSize),
                uploadRecordService.getQueryWrapper(request));
        List<UploadRecordVO> voList = uploadRecordService.getUploadRecordVO(page.getRecords());
        Page<UploadRecordVO> voPage = new Page<>(current, pageSize, page.getTotalRow());
        voPage.setRecords(voList);
        return ResultUtils.success(voPage);
    }
}
