package com.sakura.boot_init.file.controller;

import cn.hutool.core.io.FileUtil;
import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import com.sakura.boot_init.support.exception.BusinessException;
import com.sakura.boot_init.file.service.OssService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 鏂囦欢鎺ュ彛
 *
 * @author sakura
 * @from sakura
 */
@RestController
@RequestMapping("/file")
@Slf4j
@ConditionalOnProperty(value = "oss.enable", havingValue = "true")
public class FileController {

    @Resource
    private OssService ossService;


    /**
     * 鏂囦欢涓婁紶
     *
     * @param multipartFile
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile) {

        validFile(multipartFile);

        String url = ossService.uploadFile(multipartFile);
        return ResultUtils.success(url);
    }

    /**
     * 鏍￠獙鏂囦欢
     *
     * @param multipartFile
     */
    private void validFile(MultipartFile multipartFile) {
        // 鏂囦欢澶у皬
        long fileSize = multipartFile.getSize();
        // 鏂囦欢鍚庣紑
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (fileSize > ONE_M) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "鏂囦欢澶у皬涓嶈兘瓒呰繃 1M");
        }
        if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "鏂囦欢绫诲瀷閿欒");
        }
    }
}



