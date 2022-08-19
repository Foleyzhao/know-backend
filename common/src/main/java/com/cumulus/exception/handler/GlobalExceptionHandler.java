package com.cumulus.exception.handler;

import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.exception.EntityNotFoundException;
import com.cumulus.utils.ThrowableUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未知的异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleException(Throwable e) {
        // 打印堆栈信息
        if (log.isErrorEnabled()) {
            log.error(ThrowableUtils.getStackTrace(e));
        }
        return buildResponseEntity(ApiError.error(e.getMessage()));
    }

    /**
     * 处理认证凭据错误异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentialsException(BadCredentialsException e) {
        // 打印堆栈信息
        String message = "坏的凭证".equals(e.getMessage()) ? "用户名或密码不正确" : e.getMessage();
        if (log.isErrorEnabled()) {
            log.error(message);
        }
        return buildResponseEntity(ApiError.error(message));
    }

    /**
     * 处理自定义异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ApiError> badRequestException(BadRequestException e) {
        // 打印堆栈信息
        if (log.isErrorEnabled()) {
            log.error(ThrowableUtils.getStackTrace(e));
        }
        return buildResponseEntity(ApiError.error(e.getStatus(), e.getMessage()));
    }

    /**
     * 处理实体已存在异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(value = EntityExistException.class)
    public ResponseEntity<ApiError> entityExistException(EntityExistException e) {
        // 打印堆栈信息
        if (log.isErrorEnabled()) {
            log.error(ThrowableUtils.getStackTrace(e));
        }
        return buildResponseEntity(ApiError.error(e.getMessage()));
    }

    /**
     * 处理实体不存在异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ApiError> entityNotFoundException(EntityNotFoundException e) {
        // 打印堆栈信息
        if (log.isErrorEnabled()) {
            log.error(ThrowableUtils.getStackTrace(e));
        }
        return buildResponseEntity(ApiError.error(NOT_FOUND.value(), e.getMessage()));
    }

    /**
     * 处理数据验证异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // 打印堆栈信息
        if (log.isErrorEnabled()) {
            log.error(ThrowableUtils.getStackTrace(e));
        }
        String[] str = Objects.requireNonNull(e.getBindingResult().getAllErrors().get(0).getCodes())[1].split("\\.");
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        String msg = "不能为空";
        if (msg.equals(message)) {
            message = str[1] + ":" + message;
        }
        return buildResponseEntity(ApiError.error(message));
    }

    /**
     * 异常统一返回
     *
     * @param apiError API异常返回结果
     * @return 响应
     */
    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, HttpStatus.valueOf(apiError.getStatus()));
    }

    /**
     * 无权限异常处理
     *
     * @param exception API异常
     * @return 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleException(AccessDeniedException exception) {
        String message = exception.getLocalizedMessage();
        log.error("全局异常捕获AccessDeniedException：{}", message);
        return buildResponseEntity(ApiError.error(HttpStatus.FORBIDDEN.value(), message));
    }
}
