package com.cumulus.exception;

/**
 * 自定义错误配置信息异常
 */
public class BadConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1856077684294647454L;

    /**
     * 构造方法
     */
    public BadConfigurationException() {
        super();
    }

    /**
     * 构造方法
     *
     * @param message 异常信息
     */
    public BadConfigurationException(String message) {
        super(message);
    }

    /**
     * 构造方法
     *
     * @param message 异常信息
     * @param cause   异常
     */
    public BadConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造方法
     *
     * @param cause 异常
     */
    public BadConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造方法
     *
     * @param message            异常信息
     * @param cause              异常
     * @param enableSuppression  是否开启抑制
     * @param writableStackTrace 堆栈跟踪是否应该可写
     */
    protected BadConfigurationException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
