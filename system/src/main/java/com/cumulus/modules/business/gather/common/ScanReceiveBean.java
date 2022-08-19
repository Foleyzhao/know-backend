package com.cumulus.modules.business.gather.common;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cumulus.config.thread.TheadFactoryName;
import com.cumulus.modules.business.config.RabbitMqConfig;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.handler.ScanHandlerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 远程扫描任务 消费者
 *
 * @author shijh
 */
@Slf4j
@Component
public class ScanReceiveBean {

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper MPPER = new ObjectMapper();
    /**
     * 待处理队列
     */
    private static BlockingQueue<Map<String, Object>> scanQueue = new LinkedBlockingQueue<>();
    /**
     * 线程池
     */
    @Autowired
    private final TheadFactoryName theadFactory = new TheadFactoryName();

    /**
     * 采集任务接收服务接口
     */
    @Autowired
    private ScanHandlerService scanHandlerService;

    /**
     * 监听异步请求
     *
     * @param message 异步回应
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMqConfig.SCAN_DETECT_RECEIVE_TASK_QUEUE, durable = "true"),
            key = RabbitMqConfig.SCAN_DETECT_RECEIVE_ROUTING_KEY,
            exchange = @Exchange(value = RabbitMqConfig.DETECT_TASK_EXCHANGE, type = ExchangeTypes.TOPIC)))
    public void receiveDetect(Message message) {
        if (log.isInfoEnabled()) {
            log.info("Message received from 'asset.detectTaskExchange/asset.scanTaskExchange: "
                    + message.toString());
        }
        try {
            if (null == message) {
                if (log.isWarnEnabled()) {
                    log.warn("Message is null.");
                }
                return;
            }
            Map<String, Object> map = MPPER.readValue(message.getBody(), Map.class);
            //添加到队列
            scanQueue.put(map);
            log.info("map received : " + JSONObject.toJSONString(map));
            if (log.isDebugEnabled()) {
                log.debug("Receive scan response (Async): " + map.get("id"));
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Receive scan response exception", e);
            }
        }
    }

    /**
     * 线程轮询
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @PostConstruct
    private void doWhile() {
        log.info("开启轮询 receive queue");
        theadFactory.newThread(() -> {
            while (true) {
                try {
                    log.info("queue take 1");
                    scanHandlerService.handle(scanQueue.take());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
