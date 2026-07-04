package com.sakura.boot_init.file.service;

import com.sakura.boot_init.file.model.vo.UploadRecordVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传应用服务。
 *
 * @author Sakura
 */
public interface FileUploadService {

    /**
     * 上传图片并记录成功上传结果。
     *
     * @param file 图片文件
     * @param biz 图片上传业务类型
     * @return 上传结果
     */
    UploadRecordVO uploadImage(MultipartFile file, String biz);

    /**
     * 上传通用文件并记录成功上传结果。
     *
     * @param file 通用文件
     * @param biz 文件上传业务类型
     * @return 上传结果
     */
    UploadRecordVO uploadFile(MultipartFile file, String biz);
}
