package com.cumulus.modules.business.gather.common;

import javax.annotation.PostConstruct;

import com.cumulus.modules.business.config.RabbitMqConfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 远程扫描消息发送器
 *
 * @author shijh
 */
@Slf4j
@Component
public class ScanSendBean {

    /**
     * 消息通知模板对象
     */
    @Autowired
    private RabbitTemplate template;

    /**
     * 消息转换配置
     */
    @PostConstruct
    private void init() {
        template.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    /**
     * 发送异步引擎采集请求
     *
     * @param request 引擎采集请求
     */
    public void sendRequestForAsyncResponse(Object request) {
        log.info("Scan Send request:{}", request.toString());
        if (log.isDebugEnabled()) {
            log.debug("Scan Send request for async response.");
        }
        template.convertAndSend(RabbitMqConfig.DETECT_TASK_EXCHANGE, RabbitMqConfig.DETECT_SEND_ROUTING_KEY, request,
                message -> {
                    message.getMessageProperties().setReplyToAddress(
                            new Address(RabbitMqConfig.DETECT_TASK_EXCHANGE + "/" + RabbitMqConfig.SCAN_DETECT_RECEIVE_ROUTING_KEY));
                    return message;
                });
    }

}
