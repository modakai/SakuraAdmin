package com.sakura.boot_init.file.enums;

import org.apache.commons.lang3.ObjectUtils;

/**
 * 上传类型枚举。
 *
 * @author Sakura
 */
public enum FileUploadTypeEnum {

    IMAGE("图片上传", "image"),

    FILE("通用文件上传", "file");

    /**
     * 展示文案。
     */
    private final String text;

    /**
     * 存储值。
     */
    private final String value;

    FileUploadTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举。
     *
     * @param value 上传类型值
     * @return 上传类型枚举
     */
    public static FileUploadTypeEnum getEnumByValue(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (FileUploadTypeEnum anEnum : FileUploadTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public String getText() {
        return text;
    }

    public String getValue() {
        return value;
    }
}
