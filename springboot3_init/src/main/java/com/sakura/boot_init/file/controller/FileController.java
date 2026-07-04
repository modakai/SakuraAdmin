package com.sakura.boot_init.file.controller;

import com.sakura.boot_init.file.model.vo.UploadRecordVO;
import com.sakura.boot_init.file.service.FileUploadService;
import com.sakura.boot_init.shared.annotation.RateLimit;
import com.sakura.boot_init.shared.common.BaseResponse;
import com.sakura.boot_init.shared.common.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author sakura
 */
@RestController
@RequestMapping("/file")
@Slf4j
@ConditionalOnProperty(value = "oss.enable", havingValue = "true")
public class FileController {

    @Resource
    private FileUploadService fileUploadService;

    /**
     * 图片上传。
     *
     * @param multipartFile 图片文件
     * @param biz           图片上传业务类型
     * @return 上传结果
     */
    @RateLimit(prefix = "upload:image", key = "#biz", limit = 20, windowSeconds = 60)
    @PostMapping("/image/upload")
    public BaseResponse<UploadRecordVO> uploadImage(@RequestPart("file") MultipartFile multipartFile,
                                                    @RequestParam("biz") String biz) {
        return ResultUtils.success(fileUploadService.uploadImage(multipartFile, biz));
    }

    /**
     * 通用文件上传。
     *
     * @param multipartFile 上传文件
     * @param biz           文件上传业务类型
     * @return 上传结果
     */
    @RateLimit(prefix = "upload:file", key = "#biz", limit = 20, windowSeconds = 60)
    @PostMapping("/upload")
    public BaseResponse<UploadRecordVO> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                                   @RequestParam("biz") String biz) {
        return ResultUtils.success(fileUploadService.uploadFile(multipartFile, biz));
    }
}
