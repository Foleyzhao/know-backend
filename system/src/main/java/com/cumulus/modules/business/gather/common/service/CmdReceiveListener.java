package com.cumulus.modules.business.gather.common.service;

import com.cumulus.modules.business.gather.request.TaskResponse;

/**
 * 监听采集指令执行结果回应的监听器
 *
 * @author zhaoff
 */
public interface CmdReceiveListener {

    /**
     * 返回是否对特定采集引擎响应的消息感兴趣
     *
     * @param id 采集引擎响应id
     * @return 是否感兴趣
     */
    boolean interest(String id);

    /**
     * 接收并处理采集引擎响应消息
     *
     * @param response 采集引擎响应消息
     * @return 处理结果
     */
    boolean receiveAndHandle(TaskResponse response);

}
