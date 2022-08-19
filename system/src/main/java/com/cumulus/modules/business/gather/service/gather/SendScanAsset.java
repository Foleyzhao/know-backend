package com.cumulus.modules.business.gather.service.gather;

import javax.annotation.PostConstruct;
import com.cumulus.config.thread.TheadFactoryName;
import com.cumulus.modules.business.gather.common.ScanSendBean;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 远程扫描-发送
 *
 * @author shijh
 */
@Slf4j
@Component
public class SendScanAsset {

    /**
     * 线程池
     */
    @Autowired
    private final TheadFactoryName theadFactory = new TheadFactoryName();

    /**
     * 远程扫描-消息发送器
     */
    @Autowired
    private ScanSendBean scanSendBean;

    /**
     * 线程轮询
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @PostConstruct
    private void doWhile() {
        log.info("开启轮询 Scan receive queue");
        theadFactory.newThread(() -> {
            while (true) {
                try {
                    log.info("Scan queue take ");
                    scanSendBean.sendRequestForAsyncResponse(GatherTaskManager.scanQueue.take());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
