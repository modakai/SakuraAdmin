package com.sakura.boot_init.support.exception;

import com.sakura.boot_init.support.common.ErrorCode;

/**
 * 鑷畾涔夊紓甯哥被
 *
 * @author Sakura
 */
public class BusinessException extends RuntimeException {

    /**
     * 閿欒鐮?     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public int getCode() {
        return code;
    }
}




