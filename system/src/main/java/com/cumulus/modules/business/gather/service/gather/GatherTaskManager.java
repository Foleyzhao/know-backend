package com.cumulus.modules.business.gather.service.gather;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import com.alibaba.fastjson.JSON;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.detect.dto.DetectRequest;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.entity.RemoteScan;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.entity.es.GatherTaskLogEs;
import com.cumulus.modules.business.gather.entity.mysql.GatherPeriod;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import com.cumulus.modules.business.gather.entity.mysql.GatherTaskStatus;
import com.cumulus.modules.business.gather.entity.mysql.GatherType;
import com.cumulus.modules.business.gather.repository.GatherPeriodRepository;
import com.cumulus.modules.business.gather.repository.GatherPlanRepository;
import com.cumulus.modules.business.gather.repository.GatherTaskLogRepository;
import com.cumulus.modules.business.gather.request.GatherTaskRequest;
import com.cumulus.modules.business.gather.service.GatherAssetLogEsService;
import com.cumulus.modules.business.gather.service.GatherPlanService;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.GatherTaskStatusRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * ???????????????
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GatherTaskManager {

    /**
     * ??????-?????????????????????????????????
     */
    private static final String THREAD_INIT_PLAN = "GatherInitPlanThread";

    /**
     * ??????-???????????????????????????????????????
     */
    private static final String START_GATHER_THREAD = "StartGatherThread-";

    /**
     * quartz???????????????
     */
    private static final String GATHER_FORMAT = "gather-%s-%s";

    /**
     * ?????????-???????????????????????????
     */
    private static final String THREAD_SCHEDULER_PREFIX = "Gather-scheduler-%d";

    /**
     * ????????????
     */
    public static BlockingQueue<DetectRequest> scanQueue = new LinkedBlockingQueue<>();

    /**
     * ?????????????????????ID
     */
    private final AtomicLong executeThreadId = new AtomicLong(0);

    /**
     * ?????????????????????????????????
     */
    private ExecutorService taskExecutorService = null;

    /**
     * quartz ??????????????????
     */
    private Scheduler scheduler;

    /**
     * ???????????????????????????
     */
    @Autowired
    private GatherTaskRunner taskRunner;

    /**
     * ????????????????????????
     */
    @Autowired
    private GatherPlanService gatherPlanService;

    /**
     * ????????????????????????
     */
    @Autowired
    private GatherCenter gatherCenter;

    /**
     * ??????????????????????????????
     */
    @Autowired
    private GatherPlanRepository gatherPlanRepository;

    /**
     * ??????????????????????????????
     */
    @Autowired
    private GatherPeriodRepository gatherPeriodRepository;

    /**
     * ???????????????????????????????????????
     */
    @Autowired
    private GatherTaskLogRepository gatherTaskLogRepository;

    /**
     * ??????????????????????????????
     */
    @Autowired
    private GatherAssetLogEsService gatherAssetLogEsService;

    /**
     * ES??????
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * ????????????????????????
     */
    @Autowired
    private GatherTaskStatusRepository gatherTaskStatusRepository;

    /**
     * ????????????????????????
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * ???????????????
     */
    @PostConstruct
    public void init() {
        if (log.isInfoEnabled()) {
            log.info("Gather task manager init.");
        }
        ThreadFactoryBuilder threadFactoryBuilder = new ThreadFactoryBuilder();
        threadFactoryBuilder.setNameFormat(THREAD_SCHEDULER_PREFIX);
        threadFactoryBuilder.setDaemon(true);
        taskExecutorService = Executors.newFixedThreadPool(CommUtils.getGatherTaskNum(), threadFactoryBuilder.build());
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Gather job start executor error.", e);
            }
        }
        // ????????????????????????????????????
        Thread initThread = new Thread(() -> {
            // ?????????????????????
            boolean stateUpdated = false;
            while (!stateUpdated) {
                try {
                    updateGatherState();
                    stateUpdated = true;
                } catch (Exception e) {
                    int wait = 1000;
                    if (e instanceof NoNodeAvailableException) {
                        wait = 30000;
                    }
                    try {
                        Thread.sleep(wait);
                    } catch (InterruptedException ie) {
                        if (log.isWarnEnabled()) {
                            log.warn("Initialization thread is interrupted.", ie);
                        }
                    }
                    if (log.isWarnEnabled()) {
                        log.warn("Update gather state exception.", e);
                    }
                }
            }

            try {
                List<GatherPlan> plans = gatherPlanRepository.findAll();
                for (GatherPlan plan : plans) {
                    loadGatherTask(plan, false, null);
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Gather update abnormal plan error.", e);
                }
            }
        });
        initThread.setName(THREAD_INIT_PLAN);
        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * ????????????????????????????????????
     */
    private void updateGatherState() {
        Date endTime = new Date();
        // ????????????GatherAssetLog???????????????????????????
        List<GatherAssetLogEs> gatherAssetLogs = new ArrayList<>();
        // ??????????????????
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.mustNot(QueryBuilders.existsQuery("end"));
        Query query = queryBuilder.withQuery(boolQueryBuilder).build();
        SearchHits<GatherAssetLogEs> hits = esTemplate.search(query, GatherAssetLogEs.class);
        hits.forEach(hit -> {
            gatherAssetLogs.add(hit.getContent());
        });

        if (!CommUtils.isEmptyOfCollection(gatherAssetLogs)) {
            for (GatherAssetLogEs gatherAssetLog : gatherAssetLogs) {
                gatherAssetLog.setResult(GatherConstants.RESULT_FAIL);
                if (null == gatherAssetLog.getBegin()) {
                    // ??????????????????????????????????????????????????????
                    gatherAssetLog.setBegin(endTime);
                }
                if (null == gatherAssetLog.getEnd()) {
                    gatherAssetLog.setEnd(endTime);
                }
                gatherAssetLog.setState(GatherConstants.STATE_CANCELLING);
                gatherAssetLog.setReason("?????????");
            }
            gatherAssetLogEsService.saveAll(gatherAssetLogs);
        }

        // ????????????GatherTaskLog???????????????????????????2,3,4
        List<Integer> planStatus = Arrays.asList(GatherConstants.STATE_PROCESSING, GatherConstants.STATE_CANCELABLE,
                GatherConstants.STATE_CANCELLING);
        List<GatherTaskLogEs> gatherTaskLogs = gatherTaskLogRepository.findByTaskStateIn(planStatus);

        if (!CommUtils.isEmptyOfCollection(gatherTaskLogs)) {
            for (GatherTaskLogEs taskLog : gatherTaskLogs) {
                taskLog.setTaskState(GatherConstants.STATE_FAIL);
                if (null == taskLog.getEnd()) {
                    taskLog.setEnd(endTime);
                }
                taskLog.setReason("?????????");
                Map<Integer, Long> statResult = gatherAssetLogEsService.taskResultStatistics(taskLog.getId());
                if (!CommUtils.isEmptyOfMap(statResult)) {
                    Long succeedCount = statResult.get(GatherConstants.RESULT_SUCCESS);
                    if (null != succeedCount) {
                        taskLog.getTaskResult().put(GatherConstants.SUCCEED_COUNT, succeedCount);
                    }
                    Long failedCount = statResult.get(GatherConstants.RESULT_FAIL);
                    if (null != failedCount) {
                        taskLog.getTaskResult().put(GatherConstants.FAILED_COUNT, failedCount);
                    }
                    Long portionCount = statResult.get(GatherConstants.RESULT_PORTION);
                    if (null != portionCount) {
                        taskLog.getTaskResult().put(GatherConstants.PORTION_COUNT, portionCount);
                    }
                }
            }
            gatherTaskLogRepository.saveAll(gatherTaskLogs);
        }
    }

    /**
     * ??????
     */
    @PreDestroy
    public void destroy() {
        if (null != taskExecutorService) {
            taskExecutorService.shutdownNow();
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param planId       ????????????ID
     * @param ignoreCancel ??????????????????????????????
     * @return ???????????????????????????
     */
    public boolean isRunning(Long planId, boolean ignoreCancel) {
        return taskRunner.isRunning(planId, ignoreCancel);
    }

    /**
     * ??????????????????ID???????????????????????????????????????
     *
     * @param planId ????????????ID
     * @return ???????????????????????????
     */
    public boolean isCancelling(Long planId) {
        return taskRunner.isCancelling(planId);
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param taskRequest   ????????????
     * @param schedulerTask ?????????????????????
     */
    void executeSubTask(GatherTaskRequest taskRequest, boolean schedulerTask, AtomicBoolean sendScan) {
        if (log.isDebugEnabled()) {
            log.debug("Gather task add to task's executor.");
        }

        if (taskRunner.isRunning(taskRequest.getPlanId(), taskRequest.getTaskType())) {
            // ????????????????????????????????????????????????????????????
            if (log.isWarnEnabled()) {
                log.warn(String.format("Plan <%s> taskType <%s> is running.", taskRequest.getPlanName(),
                        taskRequest.getTaskType()));
            }
            return;
        }
        List<Asset> assetList = taskRequest.getAssetList();
        if (assetList.size() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("Gather assetList is null");
            }
        }
        if (sendScan.get()) {
            sendScan.set(false);
            for (Asset asset : assetList) {
                AssetConfig assetConfig = JSON.parseObject(asset.getConfig(), AssetConfig.class);
                GatherPlan plan = gatherPlanRepository.findById(taskRequest.getPlanId()).orElse(null);
                List<Integer> gatherType = assetConfig.getGatherType();
                if (gatherType.contains(DetectConstant.GATHER_TYPE_SCAN)) {
                    DetectRequest scanRequest = getScanRequest(assetConfig, asset);
                    try {
                        scanQueue.put(scanRequest);
                    } catch (InterruptedException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Failed to add remote scan queue");
                        }
                    }
                }
                if (!gatherType.contains(DetectConstant.GATHER_TYPE_LOGIN)) {
                    return;
                }
                asset.setProtocol(assetConfig.getLogin().getProtocol());
                asset.setAccount(assetConfig.getLogin().getAccount());
                asset.setPwd(assetConfig.getLogin().getPwd());
                asset.setPort(assetConfig.getLogin().getPort());
                assetRepository.save(asset);
                if (null == plan || GatherConstants.STATE_STOP.equals(plan.getStatus())) {
                    // ?????????????????????????????????????????????????????????????????????
                    if (log.isWarnEnabled()) {
                        log.warn(String.format("Plan <%s> is not exist or stop.", taskRequest.getPlanName()));
                    }
                    return;
                }
                GatherTaskStatus gatherTaskStatus = new GatherTaskStatus();
                gatherTaskStatus.setAssetId(asset.getId().intValue());
                gatherTaskStatus.setId(plan.getId());
                gatherTaskStatus.setCreateTime(new Date());
                gatherTaskStatus.setUpdateTime(new Date());
                GatherType gather = new GatherType();
                gather.setMessage("");
                gather.setResult(2);
                for (Integer integer : gatherType) {
                    if (integer == 1 || integer == 2) {
                        gather.setTypeName("????????????/agent");
                    }
                    if (integer == 3) {
                        gather.setTypeName("????????????");
                    }
                }
                gather.setStatus(2);
                gatherTaskStatus.setGatherType(JSON.toJSONString(gather));
                gatherTaskStatusRepository.save(gatherTaskStatus);
            }

            GatherTaskLogEs taskLog = new GatherTaskLogEs();
            taskLog.setPlanId(taskRequest.getPlanId());
            taskLog.setTaskState(GatherConstants.STATE_PROCESSING);
            taskLog.setBegin(new Date());
            taskLog.setTaskType(taskRequest.getTaskType());
            taskLog.setPlanName(taskRequest.getPlanName());
            if (schedulerTask) {
                taskLog.setExecType(GatherConstants.EXEC_TYPE_AUTO);
            } else {
                taskLog.setExecType(GatherConstants.EXEC_TYPE_MANUAL);
                taskLog.setExecutorId(taskRequest.getExecutorId());
            }
            gatherTaskLogRepository.save(taskLog);
            taskRequest.setGatherTaskLogES(taskLog);
            taskRunner.addTaskRequest(taskRequest);
            taskExecutorService.submit(() -> {
                if (schedulerTask) {
                    updateExecuteTime(taskRequest);
                }
                gatherPlanService.judgeAndUpdatePlanStatus(taskRequest.getPlanId(), true);
                taskRunner.addQueue(taskRequest);
            });
        }
    }
    /**
     * ????????????????????????
     *
     * @param assetConfig ??????????????????
     * @param asset       ??????
     */
    private DetectRequest getScanRequest(AssetConfig assetConfig, Asset asset) {
        DetectRequest scanRequest = new DetectRequest();
        RemoteScan remoteScan = assetConfig.getRemoteScan();
        scanRequest.setId(asset.getId().toString());
        if (remoteScan.getComponentIdentify() == 0) {
            scanRequest.setFp_level(remoteScan.getComponentIdentify());
        }
        scanRequest.setAssets(asset.getIp());
        if (!remoteScan.isPing()) {
            scanRequest.setPing(remoteScan.isPing());
        }
        scanRequest.setPort_only(false);
        scanRequest.setUdp_scan(remoteScan.getUdpPorts());
        return scanRequest;
    }

    /**
     * ????????????????????????
     *
     * @param plan   ????????????
     * @param userId ????????????ID
     */
    public void runNow(GatherPlan plan, Long userId) {
        if (taskRunner.isRunning(plan.getId(), true)) {
            return;
        }
        Thread runnerThread = new Thread(() -> loadGatherTask(plan, true, userId));
        runnerThread.setDaemon(true);
        runnerThread.setName(START_GATHER_THREAD + executeThreadId.getAndIncrement());
        runnerThread.start();
    }

    /**
     * ??????????????????
     *
     * @param plan   ????????????
     * @param runNow ??????????????????
     * @param userId ????????????ID
     */
    private void loadGatherTask(GatherPlan plan, boolean runNow, Long userId) {
        plan = gatherPlanService.findByIdHasAssetList(plan.getId()).orElse(null);

        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("loadGatherTask plan is null");
            }
            return;
        }

        if (!runNow && GatherConstants.EXEC_TYPE_MANUAL.equals(plan.getExecution())) {
            // ??????????????????????????????????????????????????????
            return;
        }
        List<Asset> assetList = plan.getAssetList();
        // ???????????????????????????
        Map<String, List<Asset>> taskAssetMap = new HashMap<>();
        // ???????????????????????????
        List<Asset> frequentlyAssets = new ArrayList<>();
        // ??????????????????????????????
        List<Asset> stationaryAssets = new ArrayList<>();
        // ?????????????????????????????????
        List<Asset> seldomAssets = new ArrayList<>();
        taskAssetMap.put(GatherConstants.TYPE_FREQUENTLY_ITEM.toString(), frequentlyAssets);
        taskAssetMap.put(GatherConstants.TYPE_STATIONARY_ITEM.toString(), stationaryAssets);
        taskAssetMap.put(GatherConstants.TYPE_SELDOM_ITEM.toString(), seldomAssets);
        for (Asset asset : assetList) {
            // ????????????????????????????????????
            Integer sysTypeId = asset.getAssetSysType().getId();
            Set<Integer> assetTaskTypes = gatherCenter.getCollectTypesBySysType(sysTypeId);
            if (null == assetTaskTypes) {
                assetTaskTypes = new HashSet<>();
                assetTaskTypes.add(GatherConstants.TYPE_STATIONARY_ITEM);
            }
            for (Integer collectType : assetTaskTypes) {
                if (collectType.equals(GatherConstants.TYPE_FREQUENTLY_ITEM)) {
                    frequentlyAssets.add(asset);
                } else if (collectType.equals(GatherConstants.TYPE_STATIONARY_ITEM)) {
                    stationaryAssets.add(asset);
                } else if (collectType.equals(GatherConstants.TYPE_SELDOM_ITEM)) {
                    seldomAssets.add(asset);
                }
            }
        }
        // ??????????????????
        long time = System.currentTimeMillis();
        AtomicBoolean sendScan = new AtomicBoolean(true);
        for (Map.Entry<String, List<Asset>> entry : taskAssetMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (GatherConstants.EXEC_TYPE_MANUAL.equals(plan.getExecution()) || null != userId) {
                // ???????????????????????????????????????????????????????????????
                runNowExecuteSubTask(plan, entry.getKey(), entry.getValue(), userId, time, sendScan);
                continue;
            }
            // ????????????
            loadGatherSubTask(plan, entry.getKey(), entry.getValue(), sendScan);
        }
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param plan         ????????????
     * @param taskType     ??????????????????
     * @param gatherAssets ???????????????
     * @param userId       ????????????ID
     */
    private void runNowExecuteSubTask(GatherPlan plan, String taskType, List<Asset> gatherAssets, Long userId, Long flag, AtomicBoolean sendScan) {
        GatherTaskRequest taskRequest = new GatherTaskRequest();
        taskRequest.setFlag(flag);
        taskRequest.setPlanId(plan.getId());
        taskRequest.setPlanName(plan.getName());
        taskRequest.setAssetList(gatherAssets);
        taskRequest.setTaskType(taskType);
        if (null != userId) {
            taskRequest.setExecutorId(userId);
        }
        executeSubTask(taskRequest, false, sendScan);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param plan         ????????????
     * @param taskType     ??????????????????
     * @param gatherAssets ????????????
     */
    @SuppressWarnings("unchecked")
    private void loadGatherSubTask(GatherPlan plan, String taskType, List<Asset> gatherAssets,AtomicBoolean sendScan) {
        String gatherType;
        if (GatherConstants.TYPE_FREQUENTLY_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_FREQUENTLY_ITEM_STR;
        } else if (GatherConstants.TYPE_STATIONARY_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_STATIONARY_ITEM_STR;
        } else if (GatherConstants.TYPE_SELDOM_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_SELDOM_ITEM_STR;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Gather taskType error: " + taskType);
            }
            return;
        }
        try {
            String taskName = String.format(GATHER_FORMAT, plan.getId(), taskType);
            JobKey jobKey = new JobKey(taskName);
            TriggerKey triggerKey = new TriggerKey(taskName);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (null != trigger) {
                removeTask(taskName);
            }
            CalendarIntervalScheduleBuilder cisb = CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                    .withMisfireHandlingInstructionDoNothing();
            List<GatherPeriod> periods = plan.getGatherPeriods();
            GatherPeriod gatherPeriod = null;
            for (GatherPeriod period : periods) {
                if (period.getContent().equals(gatherType)) {
                    gatherPeriod = period;
                }
            }
            if (null == gatherPeriod) {
                return;
            }
            ScheduleBuilder<?> sb;
            String unit = gatherPeriod.getUnit();
            Integer interval = gatherPeriod.getPeriod();
            Date scheduleDate = new Date(gatherPeriod.getStartTime().getTime());
            if (GatherConstants.SCHEDULE_GATHER_UNIT_DAY.equals(unit)) {
                sb = cisb.withIntervalInDays(interval);
            } else if (GatherConstants.SCHEDULE_GATHER_UNIT_MOUTH.equals(unit)) {
                sb = cisb.withIntervalInMonths(interval);
            } else if (GatherConstants.SCHEDULE_GATHER_UNIT_HOUR.equals(unit)) {
                sb = cisb.withIntervalInHours(interval);
            } else if (GatherConstants.SCHEDULE_GATHER_UNIT_MINUTE.equals(unit)) {
                sb = cisb.withIntervalInMinutes(interval);
            } else if (GatherConstants.SCHEDULE_GATHER_UNIT_ONCE.equals(unit)) {
                sb = SimpleScheduleBuilder.repeatHourlyForTotalCount(1)
                        .withMisfireHandlingInstructionNextWithExistingCount();
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Gather plan period not valid");
                }
                return;
            }
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(sb)
                    .startAt(scheduleDate)
                    .build();
            JobDetail jobDetail = JobBuilder.newJob(GatherTaskRunner.class).withIdentity(jobKey).build();
            jobDetail.getJobDataMap().put(GatherTaskRunner.QUARTZ_DATA_MAP_MANAGER, this);
            jobDetail.getJobDataMap().put(GatherTaskRunner.QUARTZ_DATA_MAP_PLANID, plan.getId());
            jobDetail.getJobDataMap().put(GatherTaskRunner.QUARTZ_DATA_MAP_PLANNAME, plan.getName());
            jobDetail.getJobDataMap().put("scanTask", sendScan);
            jobDetail.getJobDataMap().put(GatherTaskRunner.QUARTZ_DATA_MAP_TASKTYPE, taskType);
            jobDetail.getJobDataMap().put(GatherTaskRunner.QUARTZ_DATA_MAP_ASSETS, gatherAssets);
            scheduler.scheduleJob(jobDetail, trigger);
            // ???????????????????????????
            Date nextFireTime = trigger.getFireTimeAfter(new Date());
            if (!Objects.equals(nextFireTime, scheduleDate)) {
                gatherPeriod.setStartTime(new Timestamp(nextFireTime.getTime()));
                gatherPeriodRepository.save(gatherPeriod);
            }
            if (log.isDebugEnabled()) {
                log.debug("Gather add to quartz task successful.");
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Failed to load gather task, plan:%s, taskType:%s.", plan.getName(),
                        taskType), e);
            }
        }
    }

    /**
     * ????????????????????????????????????
     */
    private void removeTask(String taskName) {
        try {
            JobKey jobKey = new JobKey(taskName);
            TriggerKey triggerKey = new TriggerKey(taskName);
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to remove task:" + taskName, e);
            }
        }
    }

    /**
     * ??????????????????ID??????????????????
     *
     * @param planId ????????????ID
     */
    public void remove(Long planId) {
        String[] types = new String[]{GatherConstants.TYPE_FREQUENTLY_ITEM.toString(),
                GatherConstants.TYPE_STATIONARY_ITEM.toString(), GatherConstants.TYPE_SELDOM_ITEM.toString()};
        for (String type : types) {
            removeTask(String.format(GATHER_FORMAT, planId, type));
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param planId ????????????ID
     */
    public boolean cancel(Long planId) {
        return taskRunner.cancelGatherTask(planId);
    }

    /**
     * ??????????????????????????????
     *
     * @param taskRequest ??????????????????
     */
    @SuppressWarnings("unchecked")
    private synchronized void updateExecuteTime(GatherTaskRequest taskRequest) {
        Long planId = taskRequest.getPlanId();
        String taskType = taskRequest.getTaskType();
        String gatherType;
        if (GatherConstants.TYPE_FREQUENTLY_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_FREQUENTLY_ITEM_STR;
        } else if (GatherConstants.TYPE_STATIONARY_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_STATIONARY_ITEM_STR;
        } else if (GatherConstants.TYPE_SELDOM_ITEM.toString().equals(taskType)) {
            gatherType = GatherConstants.TYPE_SELDOM_ITEM_STR;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Gather taskType error: " + taskType);
            }
            return;
        }
        GatherPlan plan = gatherPlanRepository.findById(planId).orElse(null);
        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("Gather plan is not exist: " + planId);
            }
            return;
        }
        List<GatherPeriod> periods = plan.getGatherPeriods();
        GatherPeriod gatherPeriod = null;
        for (GatherPeriod period : periods) {
            if (period.getContent().equals(gatherType)) {
                gatherPeriod = period;
            }
        }
        if (null == gatherPeriod) {
            return;
        }
        String type = gatherPeriod.getUnit();
        Integer interval = gatherPeriod.getPeriod();
        Date scheduleDate = new Date(gatherPeriod.getStartTime().getTime());
        if (!StringUtils.isEmpty(type) && null != interval) {
            boolean valid = false;
            Calendar cal = Calendar.getInstance();
            while (new Date().getTime() > scheduleDate.getTime()) {
                cal.setTime(scheduleDate);
                valid = true;
                switch (type) {
                    case GatherConstants.SCHEDULE_GATHER_UNIT_MOUTH:
                        cal.add(Calendar.MONTH, interval);
                        break;
                    case GatherConstants.SCHEDULE_GATHER_UNIT_DAY:
                        cal.add(Calendar.DATE, interval);
                        break;
                    case GatherConstants.SCHEDULE_GATHER_UNIT_HOUR:
                        cal.add(Calendar.HOUR, interval);
                        break;
                    case GatherConstants.SCHEDULE_GATHER_UNIT_MINUTE:
                        cal.add(Calendar.MINUTE, interval);
                        break;
                    default:
                        valid = false;
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("Plan:%s, taskType:%s, period type %s invalid.", plan.getName(),
                                    gatherType, type));
                        }
                        break;
                }
                scheduleDate = cal.getTime();
            }
            if (valid) {
                gatherPeriod.setStartTime(new Timestamp(scheduleDate.getTime()));
            }
        }
        gatherPeriodRepository.save(gatherPeriod);
    }

}
