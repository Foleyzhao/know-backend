package com.cumulus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * 自定义异常
 */
@Getter
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = -2573133122444920364L;

    /**
     * 提示码
     */
    public static final String HINT = "当前任务消耗资源过多，建议拆分成N项任务";

    /**
     * 状态码
     */
    private Integer status = BAD_REQUEST.value();

    /**
     * 构造方法
     *
     * @param msg 异常信息
     */
    public BadRequestException(String msg) {
        super(msg);
    }

    /**
     * 构造方法
     *
     * @param status 状态码
     * @param msg    异常信息
     */
    public BadRequestException(HttpStatus status, String msg) {
        super(msg);
        this.status = status.value();
    }
}
