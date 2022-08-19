package com.cumulus.modules.business.detect.common;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import com.cumulus.config.thread.TheadFactoryName;
import com.cumulus.modules.business.detect.common.service.DetectReceiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 发现任务 消费者
 *
 * @author zhangxq
 */
@Slf4j
@Component
public class DetectReceiveBean {

    /**
     * 待处理队列
     */
    private static BlockingQueue<Map<String, Object>> queue = new LinkedBlockingQueue<>();

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper MPPER = new ObjectMapper();

    /**
     * 线程池
     */
    @Autowired
    private final TheadFactoryName theadFactory = new TheadFactoryName();

    /**
     * 发现任务接收服务接口
     */
    @Autowired
    private DetectReceiveService detectReceiveService;

    /**
     * 监听异步请求
     *
     * @param message 异步回应
     */
    @RabbitListener(queues = "asset.detectTaskQueue.receive")
    public void receiveDetect(Message message) {
        if (log.isDebugEnabled()) {
            log.debug("Message received from 'asset.detectTaskExchange/detectTaskManage.receive: "
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
            queue.put(map);
            log.info("Message received : " + message.toString());
            if (log.isDebugEnabled()) {
                log.debug("Receive detect response (Async): " + map.get("id"));
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Receive detect response exception", e);
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
                    detectReceiveService.receiveAndHandle(queue.take());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
