package com.cumulus.modules.mnt.websocket;

/**
 * Websocket 消息类型枚举类
 */
public enum MsgType {

    /**
     * 连接
     */
    CONNECT,

    /**
     * 关闭
     */
    CLOSE,

    /**
     * 信息
     */
    INFO,

    /**
     * 错误
     */
    ERROR

}
