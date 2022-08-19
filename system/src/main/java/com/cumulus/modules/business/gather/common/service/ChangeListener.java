package com.cumulus.modules.business.gather.common.service;

import java.util.Map;

/**
 * 监听变化通知的业务接口
 *
 * @author zhaoff
 */
public interface ChangeListener {

    /**
     * 缺省优先级
     */
    int DEFAULT_PRIORITY = 10;

    /**
     * 返回感兴趣的 RoutingKey 前缀
     *
     * @return 感兴趣的 RoutingKey 前缀，可以为多个
     */
    String[] getKeyPrefixes();

    /**
     * 当收到变化消息时，该方法将被调用，传入变化的内容
     *
     * @param data       变化的消息内容
     * @param routingKey 路有键值
     */
    void onChange(Map<?, ?> data, String routingKey);

    /**
     * 返回该 Listener 被调用的优先级
     *
     * @return 该 Listener 被调用的优先级
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }

}
