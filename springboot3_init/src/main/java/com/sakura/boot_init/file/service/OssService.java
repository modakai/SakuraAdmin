package com.sakura.boot_init.file.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.sakura.boot_init.support.context.LoginUserContext;
import com.sakura.boot_init.support.config.OssConfig;
import com.sakura.boot_init.user.model.entity.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@ConditionalOnProperty(value = "oss.enable", havingValue = "true")
public class OssService {

    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    @Resource
    private OSS ossClient;

    @Resource
    private OssConfig.OssProperties ossProperties;

    /**
     * 閫氱敤涓婁紶鏂规硶锛氫笂浼犳枃浠跺苟杩斿洖鍙闂殑 URL
     * @param uploadFile 瑕佷笂浼犵殑鏂囦欢
     * @param objectName 涓婁紶鍒?OSS 鐨勫璞″悕锛堜緥濡傦細images/2025/10/avatar.png锛?
     * @return 鍙叕寮€璁块棶鐨?URL锛圚TTPS锛?
     */
    public String uploadFile(File uploadFile, String objectName) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectName, uploadFile);
            // 鎵ц涓婁紶
            return upload(objectName, putObjectRequest);
        } catch (Exception e) {
            log.error("OSS 鏂囦欢涓婁紶澶辫触锛宱bjectName: {}, 閿欒: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("鏂囦欢涓婁紶澶辫触", e);
        }
    }

    /**
     * 閫氱敤涓婁紶鏂规硶锛氫笂浼犳枃浠跺苟杩斿洖鍙闂殑 URL
     *
     * @param file       瑕佷笂浼犵殑鏂囦欢锛圫pring 鐨?MultipartFile锛?
     * @return 鍙叕寮€璁块棶鐨?URL锛圚TTPS锛?
     */
    public String uploadFile(MultipartFile file) {
        User loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null || loginUser.getId() == null) {
            throw new RuntimeException("未获取到登录用户信息，上传失败");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new RuntimeException("鏂囦欢鍚嶄负绌猴細涓婁紶澶辫触");
        }
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String objectName = String.format("images/%d/%s/%s", loginUser.getId(), datePath, originalFilename);
        try (InputStream inputStream = file.getInputStream()) {
            // 鏋勯€犱笂浼犺姹?
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectName, inputStream);
            // 鎵ц涓婁紶
            return upload(objectName, putObjectRequest);
        } catch (Exception e) {
            log.error("OSS 鏂囦欢涓婁紶澶辫触锛宱bjectName: {}, 閿欒: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("鏂囦欢涓婁紶澶辫触", e);
        }
    }

    /**
     * 閲嶈浇鏂规硶锛氭敮鎸佺洿鎺ヤ紶鍏?InputStream 鍜?contentType锛堝彲閫夛級
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType) {
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(ossProperties.getBucketName(), objectName, inputStream);
            if (contentType != null && !contentType.isEmpty()) {
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(contentType);
                putObjectRequest.setMetadata(objectMetadata);
            }
            return upload(objectName, putObjectRequest);
        } catch (Exception e) {
            log.error("OSS 鏂囦欢涓婁紶澶辫触锛宱bjectName: {}, 閿欒: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("鏂囦欢涓婁紶澶辫触", e);
        }
    }

    private String upload(String objectName, PutObjectRequest putObjectRequest) {
        ossClient.putObject(putObjectRequest);
        String url = String.format("https://%s.%s/%s", ossProperties.getBucketName(), ossProperties.getEndpoint(), objectName);
        log.info("鏂囦欢涓婁紶鎴愬姛锛岃闂湴鍧€: {}", url);
        return url;
    }
}




