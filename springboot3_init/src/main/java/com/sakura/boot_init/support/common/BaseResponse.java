package com.sakura.boot_init.support.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 閫氱敤杩斿洖绫? *
 * @param <T>
 * @author sakura
 * @from sakura
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}


