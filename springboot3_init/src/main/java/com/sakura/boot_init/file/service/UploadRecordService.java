package com.sakura.boot_init.file.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.sakura.boot_init.file.model.dto.UploadRecordQueryRequest;
import com.sakura.boot_init.file.model.entity.UploadRecord;
import com.sakura.boot_init.file.model.vo.UploadRecordVO;

import java.util.List;

/**
 * 上传记录服务。
 *
 * @author Sakura
 */
public interface UploadRecordService extends IService<UploadRecord> {

    /**
     * 构建上传记录查询条件。
     *
     * @param request 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UploadRecordQueryRequest request);

    /**
     * 转换上传记录视图对象。
     *
     * @param uploadRecord 上传记录实体
     * @return 上传记录视图对象
     */
    UploadRecordVO getUploadRecordVO(UploadRecord uploadRecord);

    /**
     * 批量转换上传记录视图对象。
     *
     * @param uploadRecords 上传记录实体列表
     * @return 上传记录视图对象列表
     */
    List<UploadRecordVO> getUploadRecordVO(List<UploadRecord> uploadRecords);
}
