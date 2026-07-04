package com.sakura.boot_init.file.model.vo;

import com.sakura.boot_init.file.model.entity.UploadRecord;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 上传记录视图对象。
 *
 * @author Sakura
 */
@Data
@AutoMapper(target = UploadRecord.class)
public class UploadRecordVO {

    /**
     * 上传记录 id。
     */
    private Long id;

    /**
     * 上传用户 id。
     */
    private Long userId;

    /**
     * 上传类型：image/file。
     */
    private String uploadType;

    /**
     * 上传业务类型。
     */
    private String biz;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * OSS 对象名。
     */
    private String objectName;

    /**
     * 文件访问地址。
     */
    private String url;

    /**
     * 文件后缀。
     */
    private String fileSuffix;

    /**
     * 请求中的 Content-Type。
     */
    private String contentType;

    /**
     * 文件大小，单位字节。
     */
    private Long fileSize;

    /**
     * 上传时间。
     */
    private Date createTime;
}
