package com.cumulus.exception.handler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 自定义API异常返回结果
 */
@Data
class ApiError {

    /**
     * 响应码
     */
    private Integer status = 400;

    /**
     * 时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 构造方法
     */
    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    /**
     * 生成API异常返回结果
     *
     * @param message 异常信息
     * @return API异常返回结果
     */
    public static ApiError error(String message) {
        ApiError apiError = new ApiError();
        apiError.setMessage(message);
        return apiError;
    }

    /**
     * 生成API异常返回结果
     *
     * @param status  响应码
     * @param message 异常信息
     * @return API异常返回结果
     */
    public static ApiError error(Integer status, String message) {
        ApiError apiError = new ApiError();
        apiError.setStatus(status);
        apiError.setMessage(message);
        return apiError;
    }
}


