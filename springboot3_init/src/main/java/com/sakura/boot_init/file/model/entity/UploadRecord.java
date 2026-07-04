package com.sakura.boot_init.file.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 上传成功记录实体。
 *
 * @author Sakura
 */
@Data
@Table("sys_upload_record")
public class UploadRecord implements Serializable {

    /**
     * 主键 id。
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
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
     * 创建时间。
     */
    private Date createTime;

    /**
     * 更新时间。
     */
    private Date updateTime;

    /**
     * 逻辑删除标记。
     */
    @Column(isLogicDelete = true)
    private Integer isDelete;

    /**
     * 序列化版本号。
     */
    @Column(ignore = true)
    private static final long serialVersionUID = 1L;
}
