package com.sakura.boot_init.shared.exception;

public class ServiceException extends RuntimeException {
    /**
     * 閿欒鎻愮ず
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


