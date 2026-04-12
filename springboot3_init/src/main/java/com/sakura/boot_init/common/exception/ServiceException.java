package com.sakura.boot_init.common.exception;

public class ServiceException extends RuntimeException {
    /**
     * 错误提示
     */
    private String message;

    public ServiceException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
