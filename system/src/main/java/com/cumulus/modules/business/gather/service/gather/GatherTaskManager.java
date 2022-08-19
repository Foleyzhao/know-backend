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
 * 采集管理器
 *
 * @author zhaoff
 */
@Slf4j
@Service
public class GatherTaskManager {

    /**
     * 线程-初始化采集计划线程名称
     */
    private static final String THREAD_INIT_PLAN = "GatherInitPlanThread";

    /**
     * 线程-开始采集任务线程的名称前缀
     */
    private static final String START_GATHER_THREAD = "StartGatherThread-";

    /**
     * quartz任务名前缀
     */
    private static final String GATHER_FORMAT = "gather-%s-%s";

    /**
     * 线程池-定时调度线程名前缀
     */
    private static final String THREAD_SCHEDULER_PREFIX = "Gather-scheduler-%d";

    /**
     * 发送队列
     */
    public static BlockingQueue<DetectRequest> scanQueue = new LinkedBlockingQueue<>();

    /**
     * 采集计划的线程ID
     */
    private final AtomicLong executeThreadId = new AtomicLong(0);

    /**
     * 采集任务生成并发调度器
     */
    private ExecutorService taskExecutorService = null;

    /**
     * quartz 任务调度实体
     */
    private Scheduler scheduler;

    /**
     * 采集任务的执行实体
     */
    @Autowired
    private GatherTaskRunner taskRunner;

    /**
     * 采集计划服务接口
     */
    @Autowired
    private GatherPlanService gatherPlanService;

    /**
     * 采集指标维护中心
     */
    @Autowired
    private GatherCenter gatherCenter;

    /**
     * 采集任务数据访问接口
     */
    @Autowired
    private GatherPlanRepository gatherPlanRepository;

    /**
     * 采集周期数据访问接口
     */
    @Autowired
    private GatherPeriodRepository gatherPeriodRepository;

    /**
     * 采集任务日志数据访问层接口
     */
    @Autowired
    private GatherTaskLogRepository gatherTaskLogRepository;

    /**
     * 资产采集日志服务接口
     */
    @Autowired
    private GatherAssetLogEsService gatherAssetLogEsService;

    /**
     * ES模板
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 采集状态数据访问
     */
    @Autowired
    private GatherTaskStatusRepository gatherTaskStatusRepository;

    /**
     * 采集状态数据访问
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * 初始化方法
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
        // 系统重启后，中断处理线程
        Thread initThread = new Thread(() -> {
            // 更新状态和时间
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
     * 系统启动时，更新采集状态
     */
    private void updateGatherState() {
        Date endTime = new Date();
        // 被中断的GatherAssetLog的结束执行时间为空
        List<GatherAssetLogEs> gatherAssetLogs = new ArrayList<>();
        // 构建搜索条件
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
                    // 未开始的，设置开始时间和结束时间相同
                    gatherAssetLog.setBegin(endTime);
                }
                if (null == gatherAssetLog.getEnd()) {
                    gatherAssetLog.setEnd(endTime);
                }
                gatherAssetLog.setState(GatherConstants.STATE_CANCELLING);
                gatherAssetLog.setReason("被中断");
            }
            gatherAssetLogEsService.saveAll(gatherAssetLogs);
        }

        // 被中断的GatherTaskLog里面结果字段可能为2,3,4
        List<Integer> planStatus = Arrays.asList(GatherConstants.STATE_PROCESSING, GatherConstants.STATE_CANCELABLE,
                GatherConstants.STATE_CANCELLING);
        List<GatherTaskLogEs> gatherTaskLogs = gatherTaskLogRepository.findByTaskStateIn(planStatus);

        if (!CommUtils.isEmptyOfCollection(gatherTaskLogs)) {
            for (GatherTaskLogEs taskLog : gatherTaskLogs) {
                taskLog.setTaskState(GatherConstants.STATE_FAIL);
                if (null == taskLog.getEnd()) {
                    taskLog.setEnd(endTime);
                }
                taskLog.setReason("被中断");
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
     * 注销
     */
    @PreDestroy
    public void destroy() {
        if (null != taskExecutorService) {
            taskExecutorService.shutdownNow();
        }
    }

    /**
     * 获取采集计划是否在运行
     *
     * @param planId       采集计划ID
     * @param ignoreCancel 是否忽略任务取消状态
     * @return 采集任务是否在运行
     */
    public boolean isRunning(Long planId, boolean ignoreCancel) {
        return taskRunner.isRunning(planId, ignoreCancel);
    }

    /**
     * 根据采集计划ID判断采集计划是否正在取消中
     *
     * @param planId 采集计划ID
     * @return 计划是否正在取消中
     */
    public boolean isCancelling(Long planId) {
        return taskRunner.isCancelling(planId);
    }

    /**
     * 将启动的采集子任务加入到执行队列
     *
     * @param taskRequest   任务请求
     * @param schedulerTask 是否是定期任务
     */
    void executeSubTask(GatherTaskRequest taskRequest, boolean schedulerTask, AtomicBoolean sendScan) {
        if (log.isDebugEnabled()) {
            log.debug("Gather task add to task's executor.");
        }

        if (taskRunner.isRunning(taskRequest.getPlanId(), taskRequest.getTaskType())) {
            // 采集子任务正在执行，不将其加入到执行队列
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
                    // 采集计划不存在或计划暂停，不将其加入到执行队列
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
                        gather.setTypeName("登录采集/agent");
                    }
                    if (integer == 3) {
                        gather.setTypeName("远程扫描");
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
     * 构造远程扫描信息
     *
     * @param assetConfig 资产采集配置
     * @param asset       资产
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
     * 立即执行采集任务
     *
     * @param plan   采集计划
     * @param userId 执行用户ID
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
     * 执行采集任务
     *
     * @param plan   采集计划
     * @param runNow 是否立即执行
     * @param userId 执行用户ID
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
            // 非立即执行且是手动类型的任务，不加载
            return;
        }
        List<Asset> assetList = plan.getAssetList();
        // 采集类型对应的资产
        Map<String, List<Asset>> taskAssetMap = new HashMap<>();
        // 采集实时项资产列表
        List<Asset> frequentlyAssets = new ArrayList<>();
        // 采集耗时项项资产列表
        List<Asset> stationaryAssets = new ArrayList<>();
        // 采集不常变化项资产列表
        List<Asset> seldomAssets = new ArrayList<>();
        taskAssetMap.put(GatherConstants.TYPE_FREQUENTLY_ITEM.toString(), frequentlyAssets);
        taskAssetMap.put(GatherConstants.TYPE_STATIONARY_ITEM.toString(), stationaryAssets);
        taskAssetMap.put(GatherConstants.TYPE_SELDOM_ITEM.toString(), seldomAssets);
        for (Asset asset : assetList) {
            // 根据资产类型获取采集类型
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
        // 用作唯一标识
        long time = System.currentTimeMillis();
        AtomicBoolean sendScan = new AtomicBoolean(true);
        for (Map.Entry<String, List<Asset>> entry : taskAssetMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            if (GatherConstants.EXEC_TYPE_MANUAL.equals(plan.getExecution()) || null != userId) {
                // 计划类型为手动执行或执行人不为空，手动执行
                runNowExecuteSubTask(plan, entry.getKey(), entry.getValue(), userId, time, sendScan);
                continue;
            }
            // 自动执行
            loadGatherSubTask(plan, entry.getKey(), entry.getValue(), sendScan);
        }
    }

    /**
     * 立即执行采集子任务（手动执行）
     *
     * @param plan         采集计划
     * @param taskType     采集任务类型
     * @param gatherAssets 采集的资产
     * @param userId       执行用户ID
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
     * 执行采集子任务（自动执行）
     *
     * @param plan         采集任务
     * @param taskType     采集任务类型
     * @param gatherAssets 采集资产
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
            // 更新下一次执行时间
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
     * 根据任务名称删除任务调度
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
     * 根据采集计划ID删除任务调度
     *
     * @param planId 采集计划ID
     */
    public void remove(Long planId) {
        String[] types = new String[]{GatherConstants.TYPE_FREQUENTLY_ITEM.toString(),
                GatherConstants.TYPE_STATIONARY_ITEM.toString(), GatherConstants.TYPE_SELDOM_ITEM.toString()};
        for (String type : types) {
            removeTask(String.format(GATHER_FORMAT, planId, type));
        }
    }

    /**
     * 取消正在执行的采集任务
     *
     * @param planId 采集计划ID
     */
    public boolean cancel(Long planId) {
        return taskRunner.cancelGatherTask(planId);
    }

    /**
     * 更新采集任务执行时间
     *
     * @param taskRequest 采集任务请求
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
