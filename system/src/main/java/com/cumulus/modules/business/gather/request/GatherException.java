package com.cumulus.modules.business.gather.request;


/**
 * 封装采集过程中出现错误的异常
 *
 * @author zhaoff
 */
public class GatherException extends RuntimeException {

    private static final long serialVersionUID = 5677327689403993705L;

    /**
     * 默认构造函数
     */
    public GatherException() {
        super();
    }

    /**
     * 构造函数
     *
     * @param message 异常的原因
     * @param cause   异常
     */
    public GatherException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造函数
     *
     * @param message 异常的原因
     */
    public GatherException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param cause 异常
     */
    public GatherException(Throwable cause) {
        super(cause);
    }

}
