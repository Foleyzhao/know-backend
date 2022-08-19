package com.cumulus.modules.business.detect.common;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Address;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发现任务专用消息发送器
 *
 * @author zhangxq
 */
@Slf4j
@Component
public class DetectSendBean {

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
        log.info("Send request:{}", request.toString());
        if (log.isDebugEnabled()) {
            log.debug("Send request for async response.");
        }
        template.convertAndSend("asset.detectTaskExchange", "detectTaskManage.send", request,
                message -> {
                    message.getMessageProperties().setReplyToAddress(
                            new Address("asset.detectTaskExchange/detectTaskManage.receive"));
                    return message;
                });
    }

}
