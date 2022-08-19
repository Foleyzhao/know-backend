package com.cumulus.modules.business.gather.common.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.config.RabbitMqConfig;
import com.cumulus.modules.business.gather.request.TaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 采集指令专用消息发送器
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class CmdSendBean {

    /**
     * 消息通知模板对象
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    private void init() {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    /**
     * 发送异步引擎采集请求
     *
     * @param request 引擎采集请求
     */
    public void sendRequestForAsyncResponse(TaskRequest request) {
        if (log.isInfoEnabled()) {
            log.info("Send request for async response.:{}", JSONObject.toJSONString(request));
        }
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.GATHER_TASK_EXCHANGE,
                RabbitMqConfig.GATHER_SEND_ROUTING_KEY,
                request,
                message -> {
                    message.getMessageProperties().setReplyToAddress(
                            new Address(RabbitMqConfig.GATHER_TASK_EXCHANGE + "/" +
                                    RabbitMqConfig.GATHER_RECEIVE_ROUTING_KEY));
                    if (!StringUtils.isBlank(request.getExpiration())) {
                        message.getMessageProperties().setExpiration(request.getExpiration());
                    }
                    return message;
                });
    }
}
