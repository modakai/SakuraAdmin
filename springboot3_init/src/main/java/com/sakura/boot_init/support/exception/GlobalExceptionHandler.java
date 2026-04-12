package com.sakura.boot_init.support.exception;

import com.sakura.boot_init.support.common.BaseResponse;
import com.sakura.boot_init.support.common.ErrorCode;
import com.sakura.boot_init.support.common.ResultUtils;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 鍏ㄥ眬寮傚父澶勭悊鍣? *
 * @author sakura
 * @from sakura
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 澶勭悊璇锋眰浣撳弬鏁版牎楠屽紓甯搞€?     *
     * @param e 鍙傛暟鏍￠獙寮傚父
     * @return 缁熶竴閿欒鍝嶅簲
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        log.warn("MethodArgumentNotValidException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, extractBindingErrorMessage(e.getBindingResult().getFieldError()));
    }

    /**
     * 澶勭悊琛ㄥ崟鎴栧璞＄粦瀹氬弬鏁版牎楠屽紓甯搞€?     *
     * @param e 缁戝畾寮傚父
     * @return 缁熶竴閿欒鍝嶅簲
     */
    @ExceptionHandler(BindException.class)
    public BaseResponse<?> bindExceptionHandler(BindException e) {
        log.warn("BindException", e);
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, extractBindingErrorMessage(e.getBindingResult().getFieldError()));
    }

    /**
     * 澶勭悊鏂规硶绾у弬鏁版牎楠屽紓甯搞€?     *
     * @param e 鍙傛暟鏍￠獙寮傚父
     * @return 缁熶竴閿欒鍝嶅簲
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> constraintViolationExceptionHandler(ConstraintViolationException e) {
        log.warn("ConstraintViolationException", e);
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(constraintViolation -> constraintViolation.getMessage())
                .orElse("璇锋眰鍙傛暟閿欒");
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }


    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "绯荤粺閿欒");
    }

    /**
     * 鎻愬彇瀛楁鏍￠獙澶辫触鎻愮ず銆?     *
     * @param fieldError 瀛楁閿欒
     * @return 閿欒鎻愮ず
     */
    private String extractBindingErrorMessage(FieldError fieldError) {
        if (fieldError == null) {
            return "璇锋眰鍙傛暟閿欒";
        }
        return fieldError.getDefaultMessage();
    }
}



