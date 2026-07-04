package com.sakura.boot_init.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.sakura.boot_init.file.model.dto.UploadRecordQueryRequest;
import com.sakura.boot_init.file.model.entity.UploadRecord;
import com.sakura.boot_init.file.model.vo.UploadRecordVO;
import com.sakura.boot_init.file.repository.UploadRecordMapper;
import com.sakura.boot_init.file.service.UploadRecordService;
import com.sakura.boot_init.shared.common.ErrorCode;
import com.sakura.boot_init.shared.constant.CommonConstant;
import com.sakura.boot_init.shared.exception.ThrowUtils;
import io.github.linpeilie.Converter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sakura.boot_init.file.model.entity.table.UploadRecordTableDef.UPLOAD_RECORD;

/**
 * 上传记录服务实现。
 *
 * @author Sakura
 */
@Service
public class UploadRecordServiceImpl extends ServiceImpl<UploadRecordMapper, UploadRecord>
        implements UploadRecordService {

    /**
     * MapStruct Plus 转换器。
     */
    private final Converter converter;

    public UploadRecordServiceImpl(Converter converter) {
        this.converter = converter;
    }

    @Override
    public QueryWrapper getQueryWrapper(UploadRecordQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.where(UPLOAD_RECORD.USER_ID.eq(request.getUserId(), request.getUserId() != null));
        queryWrapper.and(UPLOAD_RECORD.UPLOAD_TYPE.eq(request.getUploadType(),
                StringUtils.isNotBlank(request.getUploadType())));
        queryWrapper.and(UPLOAD_RECORD.BIZ.eq(request.getBiz(), StringUtils.isNotBlank(request.getBiz())));
        queryWrapper.and(UPLOAD_RECORD.CREATE_TIME.ge(request.getStartTime(), request.getStartTime() != null));
        queryWrapper.and(UPLOAD_RECORD.CREATE_TIME.le(request.getEndTime(), request.getEndTime() != null));
        QueryColumn sortColumn = resolveSortColumn(request.getSortField());
        if (sortColumn != null) {
            queryWrapper.orderBy(sortColumn, CommonConstant.SORT_ORDER_ASC.equals(request.getSortOrder()));
        } else {
            queryWrapper.orderBy(UPLOAD_RECORD.CREATE_TIME, false).orderBy(UPLOAD_RECORD.ID, false);
        }
        return queryWrapper;
    }

    /**
     * 将前端排序字段转换为上传记录表字段。
     */
    private QueryColumn resolveSortColumn(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return null;
        }
        return switch (sortField) {
            case "id" -> UPLOAD_RECORD.ID;
            case "user_id" -> UPLOAD_RECORD.USER_ID;
            case "upload_type" -> UPLOAD_RECORD.UPLOAD_TYPE;
            case "biz" -> UPLOAD_RECORD.BIZ;
            case "original_name" -> UPLOAD_RECORD.ORIGINAL_NAME;
            case "file_suffix" -> UPLOAD_RECORD.FILE_SUFFIX;
            case "content_type" -> UPLOAD_RECORD.CONTENT_TYPE;
            case "file_size" -> UPLOAD_RECORD.FILE_SIZE;
            case "create_time" -> UPLOAD_RECORD.CREATE_TIME;
            case "update_time" -> UPLOAD_RECORD.UPDATE_TIME;
            default -> null;
        };
    }

    @Override
    public UploadRecordVO getUploadRecordVO(UploadRecord uploadRecord) {
        if (uploadRecord == null) {
            return null;
        }
        return converter.convert(uploadRecord, UploadRecordVO.class);
    }

    @Override
    public List<UploadRecordVO> getUploadRecordVO(List<UploadRecord> uploadRecords) {
        if (CollUtil.isEmpty(uploadRecords)) {
            return new ArrayList<>();
        }
        return uploadRecords.stream().map(this::getUploadRecordVO).collect(Collectors.toList());
    }
}
