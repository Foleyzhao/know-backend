package com.cumulus.modules.business.gather.common.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.config.RabbitMqConfig;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.CmdReceiveListener;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.request.CmdResponseData;
import com.cumulus.modules.business.gather.request.TaskResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 采集指令专用消息接收器
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class CmdReceiveBean {

    /**
     * 监听采集指令执行结果回应的监听器集合
     */
    private final Set<CmdReceiveListener> listeners = new LinkedHashSet<>();

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 监听采集异步请求
     *
     * @param message 异步回应
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMqConfig.GATHER_RECEIVE_TASK_QUEUE, durable = "true"),
            key = RabbitMqConfig.GATHER_RECEIVE_ROUTING_KEY,
            exchange = @Exchange(value = RabbitMqConfig.GATHER_TASK_EXCHANGE, type = ExchangeTypes.TOPIC)))
    public void handleAsyncCommandResponse(Message message) {
        if (log.isDebugEnabled()) {
            log.debug("Message received from 'asset.gatherTaskExchange/gatherTaskManage.receive: "
                    + message.toString());
        }
        try {
            if (null == message) {
                if (log.isWarnEnabled()) {
                    log.warn("Message is null.");
                }
                return;
            }
            Map<?, ?> map = mapper.readValue(message.getBody(), Map.class);
            if (log.isInfoEnabled()) {
                log.info("Message received from 'asset.gatherTaskExchange/gatherTaskManage.receive body: "
                        + JSONObject.toJSONString(map));
            }
            // 处理回应数据
            TaskResponse resp = new TaskResponse();
            resp.setId((String) map.get(GatherConstants.GATHER_RESPONSE_ID));
            // 执行结果
            resp.setRes((Integer) map.get(GatherConstants.GATHER_RESPONSE_CODE));
            // 解析encoding
            resp.setEncoding((String) map.get(GatherConstants.GATHER_RESPONSE_ENCODING));
            // 采集结果数据处理
            CmdResponseData responseData = new CmdResponseData();
            responseData.setStdout(CommUtils.stripAnsi((String) map.get(GatherConstants.GATHER_RESPONSE_STDOUT)));
            responseData.setCmd((String) map.get(GatherConstants.GATHER_RESPONSE_CMD));
            responseData.setReturnCode((Integer) map.get(GatherConstants.GATHER_RESPONSE_RETURN_CODE));
            responseData.setStderr((String) map.get(GatherConstants.GATHER_RESPONSE_STDERR));
            resp.setResponseData(responseData);
            // 时间处理
            Double stmInt = (Double) map.get(GatherConstants.GATHER_RESPONSE_START_TIME);
            long stmLong = stmInt.longValue() * 1000L;
            Double etmInt = (Double) map.get(GatherConstants.GATHER_RESPONSE_END_TIME);
            long etmLong = etmInt.longValue() * 1000L;
            resp.setStm(new Date(stmLong));
            resp.setEtm(new Date(etmLong));
            // 错误信息
            resp.setErrorMsg((String) map.get(GatherConstants.GATHER_RESPONSE_ERR_INFO));
            if (log.isDebugEnabled()) {
                log.debug("Receive command response (Async): " + resp);
            }
            // 加入消息列表
            synchronized (listeners) {
                for (CmdReceiveListener crl : listeners) {
                    if (crl.interest(resp.getId()) && crl.receiveAndHandle(resp)) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("Message '%s' is handled by '%s'", resp, crl));
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Receive gather response exception", e);
            }
        }
    }

    /**
     * 增加监听器
     *
     * @param listener 监听器
     */
    public void addListener(CmdReceiveListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                listeners.add(listener);
            }
        }
    }

    /**
     * 移除监听器
     *
     * @param listener 监听器
     */
    public void removeListener(CmdReceiveListener listener) {
        if (listener != null) {
            synchronized (listeners) {
                listeners.remove(listener);
            }
        }
    }

}
