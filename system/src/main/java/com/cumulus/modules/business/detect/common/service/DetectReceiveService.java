package com.cumulus.modules.business.detect.common.service;

import java.util.Map;

/**
 * 发现任务接收服务
 *
 * @author zhangxq
 */
public interface DetectReceiveService {

    /**
     * 返回是否对特定响应的消息感兴趣
     *
     * @param id 响应id
     * @return 是否感兴趣
     */
    boolean interest(String id);

    /**
     * 接收并处理响应消息
     *
     * @param response 响应消息
     */
    void receiveAndHandle(Map<String, Object> response);
}
