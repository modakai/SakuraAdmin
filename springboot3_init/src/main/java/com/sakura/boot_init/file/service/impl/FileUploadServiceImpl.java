package com.sakura.boot_init.file.service.impl;

import cn.hutool.core.io.FileUtil;
import com.sakura.boot_init.file.enums.FileUploadBizEnum;
import com.sakura.boot_init.file.enums.FileUploadTypeEnum;
import com.sakura.boot_init.file.model.entity.UploadRecord;
import com.sakura.boot_init.file.model.vo.UploadRecordVO;
import com.sakura.boot_init.file.service.FileUploadService;
import com.sakura.boot_init.file.service.OssService;
import com.sakura.boot_init.file.service.UploadRecordService;
import com.sakura.boot_init.infrastructure.config.OssConfig;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.context.LoginUserContext;
import com.sakura.boot_init.shared.context.LoginUserInfo;
import com.sakura.boot_init.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传应用服务实现。
 *
 * @author Sakura
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "oss.enable", havingValue = "true")
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * OSS 日期目录格式。
     */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 1MB 字节数。
     */
    private static final long ONE_MB = 1024 * 1024L;

    /**
     * 图片最大大小。
     */
    private static final long IMAGE_MAX_SIZE = 5 * ONE_MB;

    /**
     * 通用文件最大大小。
     */
    private static final long FILE_MAX_SIZE = 20 * ONE_MB;

    /**
     * 图片上传后缀白名单。
     */
    private static final Set<String> IMAGE_SUFFIXES = Set.of("jpeg", "jpg", "png", "webp", "svg");

    /**
     * 通用文件上传后缀白名单。
     */
    private static final Set<String> FILE_SUFFIXES = Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "jpeg", "jpg", "png", "webp", "svg");

    /**
     * OSS 服务。
     */
    private final OssService ossService;

    /**
     * 上传记录服务。
     */
    private final UploadRecordService uploadRecordService;

    /**
     * OSS 配置属性。
     */
    private final OssConfig.OssProperties ossProperties;

    public FileUploadServiceImpl(OssService ossService, UploadRecordService uploadRecordService,
            OssConfig.OssProperties ossProperties) {
        this.ossService = ossService;
        this.uploadRecordService = uploadRecordService;
        this.ossProperties = ossProperties;
    }

    @Override
    public UploadRecordVO uploadImage(MultipartFile file, String biz) {
        return upload(file, biz, FileUploadTypeEnum.IMAGE);
    }

    @Override
    public UploadRecordVO uploadFile(MultipartFile file, String biz) {
        return upload(file, biz, FileUploadTypeEnum.FILE);
    }

    /**
     * 执行上传并保存成功上传记录。
     */
    private UploadRecordVO upload(MultipartFile file, String biz, FileUploadTypeEnum uploadType) {
        LoginUserInfo loginUser = getRequiredLoginUser();
        FileUploadBizEnum bizEnum = validBiz(biz, uploadType);
        String originalName = validOriginalName(file);
        String suffix = validFileAndGetSuffix(file, uploadType);
        String objectName = buildObjectName(uploadType, loginUser.userId(), suffix);
        String contentType = file.getContentType();
        try (InputStream inputStream = file.getInputStream()) {
            String url = ossService.uploadFile(inputStream, objectName, contentType);
            UploadRecord uploadRecord = buildUploadRecord(file, loginUser.userId(), uploadType, bizEnum, originalName,
                    objectName, url, suffix, contentType);
            uploadRecordService.save(uploadRecord);
            return uploadRecordService.getUploadRecordVO(uploadRecord);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败，objectName: {}", objectName, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败");
        }
    }

    /**
     * 获取当前登录用户，上传接口不允许匿名访问。
     */
    private LoginUserInfo getRequiredLoginUser() {
        LoginUserInfo loginUser = LoginUserContext.getLoginUser();
        if (loginUser == null || loginUser.userId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return loginUser;
    }

    /**
     * 校验业务类型必须属于当前上传类型。
     */
    private FileUploadBizEnum validBiz(String biz, FileUploadTypeEnum uploadType) {
        FileUploadBizEnum bizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (bizEnum == null || !bizEnum.matchesUploadType(uploadType.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传业务类型错误");
        }
        return bizEnum;
    }

    /**
     * 校验原始文件名是否可用于记录。
     */
    private String validOriginalName(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (StringUtils.isBlank(originalName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }
        return originalName;
    }

    /**
     * 校验文件大小和后缀，并返回规范化后缀。
     */
    private String validFileAndGetSuffix(MultipartFile file, FileUploadTypeEnum uploadType) {
        String suffix = StringUtils.lowerCase(FileUtil.getSuffix(file.getOriginalFilename()), Locale.ROOT);
        if (StringUtils.isBlank(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件后缀不能为空");
        }
        if (FileUploadTypeEnum.IMAGE.equals(uploadType)) {
            validByRule(file, suffix, IMAGE_MAX_SIZE, IMAGE_SUFFIXES, "图片");
        } else {
            validByRule(file, suffix, FILE_MAX_SIZE, FILE_SUFFIXES, "文件");
        }
        return suffix;
    }

    /**
     * 按上传规则校验文件。
     */
    private void validByRule(MultipartFile file, String suffix, long maxSize, Set<String> suffixes, String label) {
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    String.format("%s大小不能超过 %dMB", label, maxSize / ONE_MB));
        }
        if (!suffixes.contains(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, label + "类型错误");
        }
    }

    /**
     * 构建 OSS 对象名。
     */
    private String buildObjectName(FileUploadTypeEnum uploadType, Long userId, String suffix) {
        String prefix = FileUploadTypeEnum.IMAGE.equals(uploadType) ? ossProperties.getImagePrefix()
                : ossProperties.getFilePrefix();
        String normalizedPrefix = StringUtils.defaultIfBlank(prefix, uploadType.getValue()).replaceAll("^/+", "")
                .replaceAll("/+$", "");
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        return String.format("%s/%s/%d/%s", normalizedPrefix, datePath, userId, filename);
    }

    /**
     * 构建上传成功记录。
     */
    private UploadRecord buildUploadRecord(MultipartFile file, Long userId, FileUploadTypeEnum uploadType,
            FileUploadBizEnum bizEnum, String originalName, String objectName, String url, String suffix,
            String contentType) {
        Date now = new Date();
        UploadRecord uploadRecord = new UploadRecord();
        uploadRecord.setUserId(userId);
        uploadRecord.setUploadType(uploadType.getValue());
        uploadRecord.setBiz(bizEnum.getValue());
        uploadRecord.setOriginalName(originalName);
        uploadRecord.setObjectName(objectName);
        uploadRecord.setUrl(url);
        uploadRecord.setFileSuffix(suffix);
        uploadRecord.setContentType(contentType);
        uploadRecord.setFileSize(file.getSize());
        uploadRecord.setCreateTime(now);
        uploadRecord.setUpdateTime(now);
        uploadRecord.setIsDelete(0);
        return uploadRecord;
    }
}
