package com.cumulus.modules.mnt.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 自定义Websocket消息实体
 */
@Data
@AllArgsConstructor
public class SocketMsg {

    /**
     * 消息内容
     */
    private String msg;

    /**
     * 消息类型
     */
    private MsgType msgType;

}
