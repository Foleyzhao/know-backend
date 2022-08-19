package com.cumulus.modules.business.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置（用于组件间常规通信）
 *
 * @author zhaoff
 */
@Configuration
public class RabbitMqConfig {

    /**
     * 发现任务消息MQ交换机
     */
    public static final String DETECT_TASK_EXCHANGE = "asset.detectTaskExchange";

    /**
     * 发现任务发送消息MQ队列
     */
    public static final String DETECT_SEND_TASK_QUEUE = "asset.detectTaskQueue.send";

    /**
     * 发现任务接收消息MQ队列
     */
    public static final String DETECT_RECEIVE_TASK_QUEUE = "asset.detectTaskQueue.receive";

    /**
     * 发现任务接收消息MQ队列
     */
    public static final String SCAN_DETECT_RECEIVE_TASK_QUEUE = "asset.scanDetectTaskQueue.receive";

    /**
     * 采集任务消息MQ交换机
     */
    public static final String GATHER_TASK_EXCHANGE = "asset.gatherTaskExchange";

    /**
     * 采集任务发送消息MQ队列
     */
    public static final String GATHER_SEND_TASK_QUEUE = "asset.gatherTaskQueue.send";

    /**
     * 采集任务接收消息MQ队列
     */
    public static final String GATHER_RECEIVE_TASK_QUEUE = "asset.gatherTaskQueue.receive";

    /**
     * 漏扫任务消息MQ交换机
     */
    public static final String VULNERABILITY_TASK_EXCHANGE = "asset.vulnerabilityTaskExchange";

    /**
     * 漏扫任务发送消息MQ队列
     */
    public static final String VULNERABILITY_SEND_TASK_QUEUE = "asset.vulnerabilityTaskQueue.send";

    /**
     * 漏扫任务接收消息MQ队列
     */
    public static final String VULNERABILITY_RECEIVE_TASK_QUEUE = "asset.vulnerabilityTaskQueue.receive";

    /**
     * 系统内部通知消息MQ交换机
     */
    public static final String SYSTEM_NOTIFY_EXCHANGE = "system.notifyExchange";

    /**
     * 系统内部通知消息MQ队列
     */
    public static final String SYSTEM_NOTIFY_QUEUE = "system.notifyQueue";

    /**
     * 发现任务发送消息路由
     */
    public static final String DETECT_SEND_ROUTING_KEY = "detectTaskManage.send";

    /**
     * 发现任务接收消息路由
     */
    public static final String DETECT_RECEIVE_ROUTING_KEY = "detectTaskManage.receive";

    /**
     * 发现任务接收消息路由
     */
    public static final String SCAN_DETECT_RECEIVE_ROUTING_KEY = "scanDetectTaskManage.receive";

    /**
     * 采集任务发送消息路由
     */
    public static final String GATHER_SEND_ROUTING_KEY = "gatherTaskManage.send";

    /**
     * 采集任务接收消息路由
     */
    public static final String GATHER_RECEIVE_ROUTING_KEY = "gatherTaskManage.receive";

    /**
     * 漏扫任务发送消息路由
     */
    public static final String VULNERABILITY_SEND_ROUTING_KEY = "vulnerabilityTaskManage.send";

    /**
     * 漏扫任务接收消息路由
     */
    public static final String VULNERABILITY_RECEIVE_ROUTING_KEY = "vulnerabilityTaskManage.receive";

    /**
     * 系统内部通知消息路由
     */
    public static final String SYSTEM_NOTIFY_ROUTING_KEY = "*.*";

    // --- 发现任务MQ配置 ---

    /**
     * 发现任务消息MQ交换机
     *
     * @return 发现任务消息MQ交换机
     */
    @Bean
    public TopicExchange detectTaskExchange() {
        return new TopicExchange(DETECT_TASK_EXCHANGE);
    }

    /**
     * 发现任务发送消息MQ队列
     *
     * @return 发现任务发送消息MQ队列
     */
    @Bean
    public Queue detectSendTaskQueue() {
        return new Queue(DETECT_SEND_TASK_QUEUE, true);
    }

    /**
     * 发现任务接收消息MQ队列
     *
     * @return 发现任务接收消息MQ队列
     */
    @Bean
    public Queue detectReceiveTaskQueue() {
        return new Queue(DETECT_RECEIVE_TASK_QUEUE, true);
    }

    /**
     * 发现任务接收消息MQ队列
     *
     * @return 发现任务接收消息MQ队列
     */
    @Bean
    public Queue scanReceiveTaskQueue() {
        return new Queue(SCAN_DETECT_RECEIVE_TASK_QUEUE, true);
    }
    /**
     * 绑定发现任务消息MQ交换机与发现任务发送消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding detectSendBindingDirect() {
        return BindingBuilder.bind(detectSendTaskQueue()).to(detectTaskExchange())
                .with(DETECT_SEND_ROUTING_KEY);
    }

    /**
     * 绑定发现任务消息MQ交换机与发现任务接收消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding detectReceiveBindingDirect() {
        return BindingBuilder.bind(detectReceiveTaskQueue()).to(detectTaskExchange())
                .with(DETECT_RECEIVE_ROUTING_KEY);
    }

    /**
     * 扫描消息MQ交换机与发现任务接收消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding scanReceiveBindingDirect() {
        return BindingBuilder.bind(scanReceiveTaskQueue()).to(detectTaskExchange())
                .with(SCAN_DETECT_RECEIVE_ROUTING_KEY);
    }

    // --- 采集任务MQ配置 ---

    /**
     * 采集任务消息MQ交换机
     *
     * @return 采集任务消息MQ交换机
     */
    @Bean
    public TopicExchange gatherTaskExchange() {
        return new TopicExchange(GATHER_TASK_EXCHANGE);
    }

    /**
     * 采集任务发送消息MQ队列
     *
     * @return 采集任务发送消息MQ队列
     */
    @Bean
    public Queue gatherSendTaskQueue() {
        return new Queue(GATHER_SEND_TASK_QUEUE, true);
    }

    /**
     * 采集任务接收消息MQ队列
     *
     * @return 采集任务接收消息MQ队列
     */
    @Bean
    public Queue gatherReceiveTaskQueue() {
        return new Queue(GATHER_RECEIVE_TASK_QUEUE, true);
    }

    /**
     * 绑定采集任务消息MQ交换机与采集任务发送消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding gatherSendBindingDirect() {
        return BindingBuilder.bind(gatherSendTaskQueue()).to(gatherTaskExchange())
                .with(GATHER_SEND_ROUTING_KEY);
    }

    /**
     * 绑定采集任务消息MQ交换机与采集任务接收消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding gatherReceiveBindingDirect() {
        return BindingBuilder.bind(gatherReceiveTaskQueue()).to(gatherTaskExchange())
                .with(GATHER_RECEIVE_ROUTING_KEY);
    }

    // --- 漏扫任务MQ配置 ---

    /**
     * 漏扫任务消息MQ交换机
     *
     * @return 漏扫任务消息MQ交换机
     */
    @Bean
    public TopicExchange vulnerabilityTaskExchange() {
        return new TopicExchange(VULNERABILITY_TASK_EXCHANGE);
    }

    /**
     * 漏扫任务发送消息MQ队列
     *
     * @return 漏扫任务发送消息MQ队列
     */
    @Bean
    public Queue vulnerabilitySendTaskQueue() {
        return new Queue(VULNERABILITY_SEND_TASK_QUEUE, true);
    }

    /**
     * 漏扫任务接收消息MQ队列
     *
     * @return 漏扫任务接收消息MQ队列
     */
    @Bean
    public Queue vulnerabilityReceiveTaskQueue() {
        return new Queue(VULNERABILITY_RECEIVE_TASK_QUEUE, true);
    }

    /**
     * 绑定漏扫任务消息MQ交换机与漏扫任务发送消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding vulnerabilitySendBindingDirect() {
        return BindingBuilder.bind(vulnerabilitySendTaskQueue()).to(vulnerabilityTaskExchange())
                .with(VULNERABILITY_SEND_ROUTING_KEY);
    }

    /**
     * 绑定漏扫任务消息MQ交换机与漏扫任务接收消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding vulnerabilityReceiveBindingDirect() {
        return BindingBuilder.bind(vulnerabilityReceiveTaskQueue()).to(vulnerabilityTaskExchange())
                .with(VULNERABILITY_RECEIVE_ROUTING_KEY);
    }

    // --- 系统内部通知消息MQ配置 ---

    /**
     * 系统内部通知消息MQ交换机
     *
     * @return 系统内部通知消息MQ交换机
     */
    @Bean
    public TopicExchange systemNotifyExchange() {
        return new TopicExchange(SYSTEM_NOTIFY_EXCHANGE);
    }

    /**
     * 系统内部通知消息MQ队列
     *
     * @return 系统内部通知消息MQ队列
     */
    @Bean
    public Queue systemNotifyQueue() {
        return new Queue(SYSTEM_NOTIFY_QUEUE, true);
    }

    /**
     * 绑定系统内部通知消息MQ交换机与系统内部通知消息MQ队列
     *
     * @return 绑定关系
     */
    @Bean
    public Binding systemNotifyBindingDirect() {
        return BindingBuilder.bind(systemNotifyQueue()).to(systemNotifyExchange())
                .with(SYSTEM_NOTIFY_ROUTING_KEY);
    }

}
