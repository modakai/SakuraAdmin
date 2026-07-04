package com.sakura.boot_init.file.model.dto;

import com.sakura.boot_init.shared.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 上传记录分页查询请求。
 *
 * @author Sakura
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UploadRecordQueryRequest extends PageRequest {

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
     * 上传开始时间。
     */
    private Date startTime;

    /**
     * 上传结束时间。
     */
    private Date endTime;
}
