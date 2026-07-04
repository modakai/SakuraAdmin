package com.sakura.boot_init.file.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author Sakura
 */
public enum FileUploadBizEnum {

    USER_AVATAR("用户头像", "user_avatar", FileUploadTypeEnum.IMAGE.getValue()),

    PHOTO_WALL("照片墙", "photo_wall", FileUploadTypeEnum.IMAGE.getValue()),

    IMAGE("普通图片", "image", FileUploadTypeEnum.IMAGE.getValue()),

    ATTACHMENT("通用附件", "attachment", FileUploadTypeEnum.FILE.getValue()),

    DOCUMENT("文档附件", "document", FileUploadTypeEnum.FILE.getValue()),

    IMPORT_FILE("导入文件", "import_file", FileUploadTypeEnum.FILE.getValue());

    private final String text;

    private final String value;

    private final String uploadType;

    FileUploadBizEnum(String text, String value, String uploadType) {
        this.text = text;
        this.value = value;
        this.uploadType = uploadType;
    }

    /**
     * 获取值列表
     *
     * @return 值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 枚举对象
     */
    public static FileUploadBizEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    /**
     * 判断业务类型是否属于指定上传类型。
     *
     * @param uploadType 上传类型
     * @return 是否匹配
     */
    public boolean matchesUploadType(String uploadType) {
        return this.uploadType.equals(uploadType);
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public String getUploadType() {
        return uploadType;
    }
}
