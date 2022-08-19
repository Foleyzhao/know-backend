package com.cumulus.modules.business.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 引擎专用消息发送器
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class EngineSendBean {

    /**
     * 消息通知模板对象
     */
    @Autowired
    private RabbitTemplate template;

    @PostConstruct
    private void init() {
        template.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    /**
     * 发送同步请求
     *
     * @param request 请求
     * @return 回应
     */
    public Object sendRequestForResponse(Object request) {
        Object response = template.convertSendAndReceive("scanTaskManage", request);
        if (log.isInfoEnabled()) {
            log.info("Receive engine response: " + response);
        }
        return response;
    }

    /**
     * 发送异步请求
     *
     * @param request 请求
     */
    public void sendRequestForAsyncResponse(Object request) {
        if (log.isDebugEnabled()) {
            log.debug("send request for async response");
        }
        template.convertAndSend("asset.scanTaskTopic", "scanTaskManage", request, message -> {
            // TODO 设置回复队列及Topic
            message.getMessageProperties().setReplyToAddress(new Address("asset.scanTaskTopic/scanTaskManage"));
            // TODO 可以在 request中设置超时时间
            return message;
        });
    }
}
