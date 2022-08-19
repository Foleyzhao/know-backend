package com.cumulus.modules.business.gather.common.service.impl;

import com.cumulus.modules.business.config.RabbitMqConfig;
import com.cumulus.modules.business.gather.common.service.AmqpNotificationService;
import com.cumulus.modules.business.gather.common.service.ChangeListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 内部通知使用的服务实现
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class AmqpNotificationServiceImpl implements AmqpNotificationService {

    /**
     * JAVA对象映射JSON的映射器
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 所有的消息监听器
     */
    private ChangeListener[] listeners = null;

    /**
     * 消息通知模板对象
     */
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Spring 上下文
     */
    @Autowired
    private ApplicationContext appContext;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        if (log.isDebugEnabled()) {
            log.debug("Init AmqpNotificationServiceImpl.");
        }
        List<ChangeListener> listeners = new ArrayList<>();
        Map<String, ChangeListener> map =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(appContext, ChangeListener.class);
        for (Map.Entry<String, ChangeListener> entry : map.entrySet()) {
            if (log.isDebugEnabled()) {
                log.debug("Add change listener: " + entry.getKey());
            }
            listeners.add(entry.getValue());
        }
        listeners.sort(Comparator.comparingInt(ChangeListener::getPriority));
        this.listeners = listeners.toArray(new ChangeListener[0]);
    }

    /**
     * 回收资源
     */
    @PreDestroy
    public void destroy() {
    }

    @Override
    public void sendNotification(String routingKey, Object entity) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Sending change notification, routingKey=%s.", routingKey));
        }
        try {
            rabbitTemplate.send(RabbitMqConfig.SYSTEM_NOTIFY_EXCHANGE,
                    routingKey, MessageBuilder.withBody(MAPPER.writeValueAsBytes(entity))
                            .setContentType(MediaType.APPLICATION_JSON_VALUE).build());
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Failed to send change notification, routingKey=%s.", routingKey), e);
            }
        }
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
        List<ChangeListener> list = new ArrayList<>();
        if (null != listeners) {
            for (ChangeListener cl : listeners) {
                if (cl.equals(changeListener)) {
                    // 已经存在，不再增加
                    return;
                }
                list.add(cl);
            }
        }
        list.add(changeListener);
        listeners = list.toArray(new ChangeListener[0]);
    }

    /**
     * 接收通知消息，并调用各接收者（ChangeListener）的回调方法，通知特定变更操作
     *
     * @param message    变更通知消息
     * @param routingKey 消息的路由键值
     */
    @RabbitListener(bindings = @QueueBinding(
                    value = @Queue(name = RabbitMqConfig.SYSTEM_NOTIFY_QUEUE, durable = "true"),
                    key = RabbitMqConfig.SYSTEM_NOTIFY_ROUTING_KEY,
                    exchange = @Exchange(value = RabbitMqConfig.SYSTEM_NOTIFY_EXCHANGE, type = ExchangeTypes.TOPIC)))
    public void onChangeNotification(Message message, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Receive change notification, routingKey=%s.", routingKey));
        }

        try {
            // 构造不可改变的 Map，并通知各个注册的监听者（回调）
            Map<?, ?> data = Collections.unmodifiableMap(MAPPER.readValue(message.getBody(), Map.class));
            synchronized (listeners) {
                for (ChangeListener listener : listeners) {
                    for (String keyPrefix : listener.getKeyPrefixes()) {
                        if (routingKey.startsWith(keyPrefix)) {
                            try {
                                long t0 = System.currentTimeMillis();
                                listener.onChange(data, routingKey);
                                long time = System.currentTimeMillis() - t0;
                                if (time > 500) {
                                    // 通知处理时间太长，需要优化
                                    if (log.isWarnEnabled()) {
                                        log.warn("Notification ChangeListener processing time is too long, listener="
                                                + listener + ", time(ms)=" + time);
                                    }
                                }
                            } catch (Throwable t) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to handle notification, routingKey=" + routingKey + ", listener="
                                            + listener, t);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } catch (Throwable t) {
            if (log.isErrorEnabled()) {
                log.error("Uncaptured error occurred when handle change notification", t);
            }
        }
    }

}
