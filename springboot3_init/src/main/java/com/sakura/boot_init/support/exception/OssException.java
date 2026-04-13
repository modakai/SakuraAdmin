package com.sakura.boot_init.support.exception;

import com.sakura.boot_init.support.common.ErrorCode;

/**
 * 处理 OSS 异常
 */
public class OssException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public OssException(int code, String message) {
        super(message);
        this.code = code;
    }

    public OssException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public OssException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}
