package com.cumulus.modules.business.gather.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import com.alibaba.fastjson.JSON;
import com.cumulus.enums.MessageTypeEnum;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.AmqpNotificationService;
import com.cumulus.modules.business.gather.common.service.ChangeListener;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.entity.es.GatherResultEs;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import com.cumulus.modules.business.gather.repository.GatherAssetLogEsRepository;
import com.cumulus.modules.business.gather.repository.GatherPlanRepository;
import com.cumulus.modules.business.gather.repository.GatherResultRepository;
import com.cumulus.modules.business.gather.service.GatherPlanService;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.service.MessageService;
import com.cumulus.utils.DateUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * 将采集结果放入队列中
 *
 * @author Shijh
 */
@Component
@Slf4j
public class GatherResultImpl implements ChangeListener {

    /**
     * 线程-采集分发线程名
     */
    public static final String THREAD_TASK_RESULT = "Gather-task-result";

    private static final String[] key = new String[]{AmqpNotificationService.MSG_UPDATE_FINISHED};
    /**
     * 采集任务结果队列
     */
    private final LinkedBlockingQueue<Map<?, ?>> taskResult = new LinkedBlockingQueue<>();

    /**
     * 采集结果数据访问接口
     */
    @Autowired
    private GatherResultRepository gatherResultRepository;

    /**
     * 资产采集日志数据访问接口
     */
    @Autowired
    private GatherAssetLogEsRepository gatherAssetLogEsRepository;

    /**
     * 采集任务数据访问接口
     */
    @Autowired
    private GatherPlanRepository gatherPlanRepository;

    /**
     * 消息通知服务
     */
    @Autowired
    private MessageService messageService;

    /**
     * 初始化采集结果线程
     */
    @PostConstruct
    public void init() {
        Thread gatherThread = new Thread(() -> {
            while (true) {
                try {
                    Map<?, ?> data = taskResult.take();
                    saveResult(data);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("GatherThread error result", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("GatherThread sleep error result", ex);
                        }
                    }
                }
            }
        });
        gatherThread.setDaemon(true);
        gatherThread.setName(THREAD_TASK_RESULT);
        gatherThread.start();
    }

    @Override
    public String[] getKeyPrefixes() {
        return key;
    }

    @Override
    public void onChange(Map<?, ?> data, String routingKey) {
        taskResult.add(data);
    }

    private void saveResult(Map<?, ?> data) {
        Integer planId = (Integer) data.get("planId");
        String planName = (String) data.get("planName");
        Object begin = data.get("begin");
        Integer result = (Integer) data.get("result");
        Object assetIp = data.get("assetIp");
        Object flag = data.get("flag");
        GatherResultEs gatherResultEs = gatherResultRepository.findByAssetIpAndAndPlanId(assetIp.toString(),planId);
        if (ObjectUtils.isEmpty(gatherResultEs)) {
            gatherResultEs = new GatherResultEs();
            gatherResultEs.setPlanId((planId.longValue()));
            gatherResultEs.setPlanName(planName);
        }
        List<GatherAssetLogEs> allLogs = gatherAssetLogEsRepository.findByFlagIs((Long) flag);
        for (GatherAssetLogEs log : allLogs) {
            gatherResultEs.setAssetIp(assetIp.toString());
            gatherResultEs.setBegin(new Date((Long) begin));
            if (log.getTaskType().equals(GatherConstants.TYPE_FREQUENTLY_ITEM.toString())) {
                gatherResultEs.setFrequently(result);
            }
            if (log.getTaskType().equals(GatherConstants.TYPE_STATIONARY_ITEM.toString())) {
                gatherResultEs.setStationary(result);
            }
            if (log.getTaskType().equals(GatherConstants.TYPE_SELDOM_ITEM.toString())) {
                gatherResultEs.setSeldom(result);
            }
        }
        if (gatherResultEs.getFrequently() == 0 && gatherResultEs.getStationary() == 0 && gatherResultEs.getSeldom() == 0) {
            gatherResultEs.setResult(GatherConstants.STATE_SUCCESS);
        } else if (gatherResultEs.getFrequently() == 1 && gatherResultEs.getStationary() == 1 && gatherResultEs.getSeldom() == 1) {
            gatherResultEs.setResult(GatherConstants.STATE_FAIL);
        } else {
            gatherResultEs.setResult(GatherConstants.STATE_PORTION);
        }
        gatherResultEs.setEnd(new Date());
        gatherResultEs.setGatherObj(gatherPlanRepository.findById(gatherResultEs.getPlanId()).orElse(null).getGatherNum());
        gatherResultRepository.save(gatherResultEs);
        Optional<GatherPlan> gatherPlanOpt = gatherPlanRepository.findById(Long.valueOf(planId));
        if (gatherPlanOpt.isPresent()) {
            GatherPlan gatherTask = gatherPlanOpt.get();
            if (gatherTask.getStatus() == 2) {
                //发送系统通知
                messageService.sendMessage(MessageTypeEnum.GATHER_END_TYPE, Menu.DEFAULT_GATHER_MENU_ID,
                        Collections.singletonList(gatherTask.getId().toString()), gatherTask.getName(),
                        DateUtils.SIMPLE_DFY_MD_HMS.format(new Date()));
            }
        }
    }
}
