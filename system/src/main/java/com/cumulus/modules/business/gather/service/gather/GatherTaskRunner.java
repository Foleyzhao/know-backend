package com.cumulus.modules.business.gather.service.gather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.AmqpNotificationService;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.entity.es.GatherAssetLogEs;
import com.cumulus.modules.business.gather.entity.es.GatherItemLogEs;
import com.cumulus.modules.business.gather.entity.es.GatherTaskLogEs;
import com.cumulus.modules.business.gather.model.GatherDetail;
import com.cumulus.modules.business.gather.model.GatherXmlCategoryBean;
import com.cumulus.modules.business.gather.repository.GatherAssetLogEsRepository;
import com.cumulus.modules.business.gather.repository.GatherItemLogEsRepository;
import com.cumulus.modules.business.gather.repository.GatherTaskLogRepository;
import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import com.cumulus.modules.business.gather.request.GatherCmdRequest;
import com.cumulus.modules.business.gather.request.GatherException;
import com.cumulus.modules.business.gather.request.GatherItemLog;
import com.cumulus.modules.business.gather.request.GatherTaskRequest;
import com.cumulus.modules.business.gather.service.GatherAssetLogEsService;
import com.cumulus.modules.business.gather.service.GatherItemLogEsService;
import com.cumulus.modules.business.gather.service.GatherPlanService;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.repository.UserRepository;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 资产采集执行服务对象
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class GatherTaskRunner implements Job {

    /**
     * 定时任务数据-计划ID
     */
    public static final String QUARTZ_DATA_MAP_PLANID = "planId";

    /**
     * 定时任务数据-计划名称
     */
    public static final String QUARTZ_DATA_MAP_PLANNAME = "planName";

    /**
     * 定时任务数据-计划类型
     */
    public static final String QUARTZ_DATA_MAP_TASKTYPE = "taskType";

    /**
     * 定时任务数据-计划采集资产
     */
    public static final String QUARTZ_DATA_MAP_ASSETS = "gatherAssets";

    /**
     * 定时任务数据-采集管理器
     */
    public static final String QUARTZ_DATA_MAP_MANAGER = "gatherTaskManager";

    /**
     * 线程池-采集任务并发调度线程名前缀
     */
    public static final String THREAD_GATHER_PREFIX = "Gather-Asset-%d";

    /**
     * 线程池-agent采集结构收集线程名前缀
     */
    public static final String THREAD_COLLECT_PREFIX = "Gather-Status-%d";

    /**
     * 线程-采集处理线程名
     */
    public static final String THREAD_HANDLER_NAME = "Gather-Task-handler";

    /**
     * 线程-采集分发线程名
     */
    public static final String THREAD_DISPATCH_NAME = "Gather-task-dispatch";

    /**
     * 线程-agent采集结果监控线程名
     */
    public static final String THREAD_MONITOR_NAME = "Gather-request-monitor";

    /**
     * 线程-采集处理线程名
     */
    public static final String THREAD_AGENT_NAME = "Script-gather-monitor";

    /**
     * 线程-采集清理线程名
     */
    public static final String THREAD_CLEAN_NAME = "Gather-log-clean";

    /**
     * 线程池-采集任务并发调度线程活跃时间
     */
    public static final Integer THREAD_GATHER_ALIVE_TIME = 30;
    /**
     * 采集任务任务队列
     */
    private final LinkedBlockingQueue<GatherTaskRequest> taskQueue = new LinkedBlockingQueue<>();
    /**
     * 记录采集计划对应的采集任务列表
     */
    private final ConcurrentHashMap<Long, Set<GatherTaskRequest>> planTasks = new ConcurrentHashMap<>();
    /**
     * 正在运行的采集设备任务，采集ID和资产IP的映射
     */
    private final ConcurrentHashMap<Long, Map<String, String>> planGatherIdAndIp = new ConcurrentHashMap<>();
    /**
     * 正在取消的采集计划ID集合
     */
    private final List<Long> cancellingPlans = Collections.synchronizedList(new ArrayList<>());
    /**
     * 通过agent采集结束的采集ID列表
     */
    private final List<String> agentGatherEndIds = Collections.synchronizedList(new ArrayList<>());
    /**
     * 采集锁
     */
    private final ArrayBlockingQueue<Boolean> gatherLock = new ArrayBlockingQueue<>(100);
    /**
     * 采集指标维护中心
     */
    @Autowired
    private GatherCenter gatherCenter;
    /**
     * 采集管理服务中心
     */
    @Autowired
    private GatherProviderManager gatherProviderManager;
    /**
     * 命令行输出结果的解析器
     */
    @Autowired
    private CmdOutputParser cmdOutputParser;
    /**
     * 基线检查日志记录服务
     */
    @Autowired
    private GatherItemLogEsService gatherItemLogEsService;
    /**
     * 采集数据处理服务（ES）
     */
    @Autowired
    private GatherDataEsService gatherEsService;
    /**
     * 采集任务日志数据访问接口
     */
    @Autowired
    private GatherTaskLogRepository gatherTaskLogRepository;
    /**
     * 资产采集日志数据访问接口
     */
    @Autowired
    private GatherAssetLogEsRepository gatherAssetLogEsRepository;
    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;
    /**
     * 用户数据访接口
     */
    @Autowired
    private UserRepository userRepository;
    /**
     * 资产采集项日志数据访问接口
     */
    @Autowired
    private GatherItemLogEsRepository gatherItemLogEsRepository;
    /**
     * 采集计划服务接口
     */
    @Autowired
    private GatherPlanService gatherPlanService;
    /**
     * 资产采集日志服务接口
     */
    @Autowired
    private GatherAssetLogEsService gatherAssetLogEsService;
    /**
     * 采集任务发送并发调度器
     */
    private ThreadPoolExecutor gatherExecutor = null;
    /**
     * 采集任务队列大小
     */
    private int taskQueueSize = 0;
    /**
     * 采集最大并发数量
     */
    private int gatherLimit = 0;
    /**
     * agent采集日志收集调度器
     */
    private ExecutorService collectItemLogExecutor = null;
    /**
     * 内部通知接口
     */
    @Autowired
    private AmqpNotificationService amqpNotificationService;

    /**
     * 初始化 资产采集线程池
     */
    @PostConstruct
    public void init() {
        initGatherHandleThread();

        // 采集任务分发线程
        Thread gatherThread = new Thread(() -> {
            while (true) {
                try {
                    GatherTaskRequest request = taskQueue.take();
                    doInternalSync(request);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("GatherThread error", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("GatherThread sleep error", ex);
                        }
                    }
                }
            }
        });
        gatherThread.setDaemon(true);
        gatherThread.setName(THREAD_DISPATCH_NAME);
        gatherThread.start();

        // 采集请求执行进度线程
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    monitorTaskRequest();
                    Thread.sleep(5000);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Monitor progress of task request exception.", e);
                    }
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName(THREAD_MONITOR_NAME);
        monitorThread.start();

        // TODO zhaoff agent 监控采集结果
        Thread gatherResultMonitorThread = new Thread(() -> {
            while (true) {
                try {
                    monitorScriptTask();
                    Thread.sleep(10000);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Monitor of script gather exception.", e);
                    }
                }
            }
        });
        gatherResultMonitorThread.setDaemon(true);
        gatherResultMonitorThread.setName(THREAD_AGENT_NAME);
        gatherResultMonitorThread.start();

        // 清理
        Thread itemLogEntityCleanThread = new Thread(() -> {
            while (true) {
                try {
                    gatherItemLogEsService.deleteItemLogOneDayAgo();
                    Thread.sleep(3600000);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Regular clean item log entity exception.", e);
                    }
                }
            }
        });
        itemLogEntityCleanThread.setDaemon(true);
        itemLogEntityCleanThread.setName(THREAD_CLEAN_NAME);
        itemLogEntityCleanThread.start();
    }

    /**
     * 初始化采集处理线程
     */
    private void initGatherHandleThread() {
        gatherLimit = CommUtils.getGatherJobNum();
        taskQueueSize = gatherLimit * 4;
        ThreadFactoryBuilder gatherThreadBuilder = new ThreadFactoryBuilder();
        gatherThreadBuilder.setNameFormat(THREAD_GATHER_PREFIX);
        gatherThreadBuilder.setDaemon(true);
        gatherExecutor = new ThreadPoolExecutor(gatherLimit, gatherLimit, THREAD_GATHER_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(taskQueueSize), gatherThreadBuilder.build());

        ThreadFactoryBuilder statusThreadBuilder = new ThreadFactoryBuilder();
        statusThreadBuilder.setNameFormat(THREAD_COLLECT_PREFIX);
        statusThreadBuilder.setDaemon(true);
        collectItemLogExecutor = Executors.newFixedThreadPool(CommUtils.getGatherJobNum(), statusThreadBuilder.build());

        // 采集处理线程
        Thread gatherHandleThread = new Thread(() -> {
            int gatherWait = 10;
            while (true) {
                try {
                    runGatherTask();
                    gatherWait = 10;
                } catch (Throwable e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Run gather task fail", e);
                    }
                    if (e instanceof NoNodeAvailableException) {
                        gatherWait = 60;
                    }
                } finally {
                    waitLock(gatherWait);
                }
            }
        });
        gatherHandleThread.setName(THREAD_HANDLER_NAME);
        gatherHandleThread.setDaemon(true);
        gatherHandleThread.start();
    }

    /**
     * 间隔执行一次定期轮询任务，将状态为未开始的采集任务放入线程池进行采集
     */
    private void runGatherTask() {
        if (canAddTaskToQueue()) {
            // 检测是否有正在执行的未执行成功的任务id
            ConcurrentHashMap.KeySetView<Long, Set<GatherTaskRequest>> longs = planTasks.keySet();
            Set<Long> planIds = new HashSet<>(longs);
            // 去掉正在删除的计划
            cancellingPlans.forEach(planIds::remove);
            List<GatherAssetLogEs> unGatherAssetLogList = gatherAssetLogEsService.getUnGatherTask(planIds, gatherLimit);
            if (!CommUtils.isEmptyOfCollection(unGatherAssetLogList)) {
                for (GatherAssetLogEs assetLogES : unGatherAssetLogList) {
                    GatherTaskRequest taskRequest = getTaskRequest(assetLogES.getPlanId(), assetLogES.getTaskType());
                    if (null != taskRequest && !taskRequest.isCancel()) {
                        try {
                            if (taskRequest.getQueuedTask().contains(assetLogES.getGatherId())) {
                                // 已加入线程池等待队列的，不再重复加入
                                continue;
                            }
                            assetLogES.setState(GatherConstants.STATE_RUNNING);
                            gatherAssetLogEsRepository.save(assetLogES);
                            Future<?> future = gatherExecutor.submit(new GatherSubWork(assetLogES, taskRequest));
                            taskRequest.addFuture(future);
                            taskRequest.getQueuedTask().add(assetLogES.getGatherId());
                        } catch (Exception e) {
                            if (log.isWarnEnabled()) {
                                log.warn(String.format("Failed to submit task, gatherId:%s, asset:%s.",
                                        assetLogES.getGatherId(), assetLogES.getAssetName()));
                            }
                            if (e instanceof NoNodeAvailableException) {
                                throw e;
                            }
                        }
                    } else {
                        if (log.isInfoEnabled()) {
                            log.info(String.format("Gather task cancel, plan:%s, taskType:%s, assetId:%d.",
                                    assetLogES.getPlanName(), assetLogES.getTaskType(), assetLogES.getAssetId()));
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断是否可以增加采集任务到任务队列中
     *
     * @return 是否可以增加采集任务到任务队列中（true-可以, false-不可以）
     */
    private boolean canAddTaskToQueue() {
        if (null == gatherExecutor) {
            return false;
        }
        boolean result = false;
        int waitQueue = gatherExecutor.getQueue().size();
        if (waitQueue < taskQueueSize * 0.75) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("The waiting queue length of the gather thread pool is %s.", waitQueue));
        }
        return result;
    }

    /**
     * 等待采集锁
     *
     * @param time 等待时间
     */
    private void waitLock(long time) {
        try {
            gatherLock.poll(time, TimeUnit.SECONDS);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Wait lock error", e);
            }
        }
    }

    /**
     * 释放采集锁
     */
    private void notifyLock() {
        try {
            if (gatherLock.isEmpty()) {
                boolean result = gatherLock.offer(Boolean.TRUE);
                if (log.isInfoEnabled()) {
                    log.info("Nock result is " + result);
                }
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Notify lock error", e);
            }
        }
    }

    /**
     * 监控采集任务进度
     */
    private void monitorTaskRequest() {
        synchronized (planTasks) {
            Iterator<Map.Entry<Long, Set<GatherTaskRequest>>> planIterator = planTasks.entrySet().iterator();
            while (planIterator.hasNext()) {
                Map.Entry<Long, Set<GatherTaskRequest>> plan = planIterator.next();
                Long planId = plan.getKey();
                Set<GatherTaskRequest> tmpTasks = new HashSet<>();
                if (null != plan.getValue()) {
                    tmpTasks.addAll(plan.getValue());
                }
                Iterator<GatherTaskRequest> taskIterator = tmpTasks.iterator();
                while (taskIterator.hasNext()) {
                    GatherTaskRequest request = taskIterator.next();
                    if (request.overCheck()) {
                        taskRequestHandle(request);
                        gatherPlanService.judgeAndUpdatePlanStatus(request.getPlanId(), false);
                        taskIterator.remove();
                    }
                }
                if (tmpTasks.isEmpty()) {
                    planIterator.remove();
                    planGatherIdAndIp.remove(planId);
                } else {
                    planTasks.put(plan.getKey(), tmpTasks);
                }
            }
        }
    }

    /**
     * TODO zhaoff
     * 监控脚本任务的采集流程（agent）
     */
    private void monitorScriptTask() {
        Set<Long> planIds = new HashSet<>(planTasks.keySet());
        for (Long planId : planIds) {
            if (!isRunning(planId, false)) {
                continue;
            }
            List<String> gatherIds = gatherAssetLogEsService.getRunningTasksGatherId(planId, true);
            for (String gatherId : gatherIds) {
                GatherAssetLogEs assetLog = gatherAssetLogEsRepository.findByGatherId(gatherId);
                if (null == assetLog) {
                    continue;
                }
                if (!gatherEnd(assetLog)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Gather is not over, gatherId:%s, taskType:%s, asset:%s", gatherId,
                                assetLog.getTaskType(), assetLog.getAssetName()));
                    }
                    continue;
                } else {
                    if (log.isInfoEnabled()) {
                        log.info(String.format("Agent gather end, gatherId:%s, taskType:%s, asset:%s", gatherId,
                                assetLog.getTaskType(), assetLog.getAssetName()));
                    }
                }
                if (agentGatherEndIds.contains(gatherId)) {
                    // 已采集结束并加入采集日志搜集线程池的，不再重复添加
                    continue;
                }
                agentGatherEndIds.add(gatherId);
                collectItemLogExecutor.submit(new ItemLogCollector(gatherId));
            }
        }
    }

    /**
     * TODO zhaoff
     * 判断资产采集是否结束
     *
     * @param assetLog 资产采集日志
     * @return 是否结束
     */
    private boolean gatherEnd(GatherAssetLogEs assetLog) {
        long completed = gatherItemLogEsRepository.countAllByGatherId(assetLog.getGatherId());
        if (log.isDebugEnabled()) {
            log.debug(String.format("Number of collected items is %s for gatherId:%s", completed,
                    assetLog.getGatherId()));
        }
        if (assetLog.getSize() <= completed || Objects.equals(assetLog.getResult(), GatherConstants.RESULT_FAIL)) {
            return true;
        }
        int timeout = getGatherTaskTimeout(assetLog.getTaskType()) + 15;
        long currentTime = System.currentTimeMillis();
        if (0 != assetLog.getBegin().getTime() && currentTime - assetLog.getBegin().getTime() > timeout * 1000L) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Gather timeout, gatherId:%s, asset:%s", assetLog.getGatherId(),
                        assetLog.getAssetName()));
            }
            return true;
        }
        return false;
    }

    /**
     * 获取采集任务的超时时间
     *
     * @param taskType 采集任务类型
     * @return 采集任务的超时时间
     */
    private int getGatherTaskTimeout(String taskType) {
        int timeout = CommUtils.getGatherFrequentlyTimeout();
        if (GatherConstants.TYPE_STATIONARY_ITEM.toString().equals(taskType)) {
            timeout = CommUtils.getGatherStationaryTimeout();
        } else if (GatherConstants.TYPE_SELDOM_ITEM.toString().equals(taskType)) {
            timeout = CommUtils.getGatherSeldomTimeout();
        }
        return timeout;
    }

    /**
     * 采集任务执行完毕后处理
     *
     * @param request 采集任务请求
     */
    private void taskRequestHandle(GatherTaskRequest request) {
        try {
            Long succeedCount = request.getSucceedCount().longValue();
            Long portionCount = request.getPortionCount().longValue();
            Long failedCount = request.getFailedCount().longValue();
            int assetCount = request.getAssetList().size();
            GatherTaskLogEs taskLog = request.getGatherTaskLogES();
            if (0 == request.getGatherSize()) {
                // 针对所有任务都未采集的情况
                Map<Integer, Long> statResult = gatherAssetLogEsService.taskResultStatistics(taskLog.getId());
                if (!CommUtils.isEmptyOfMap(statResult)) {
                    if (statResult.containsKey(GatherConstants.RESULT_SUCCESS)) {
                        succeedCount = statResult.get(GatherConstants.RESULT_SUCCESS);
                    }
                    if (statResult.containsKey(GatherConstants.RESULT_FAIL)) {
                        failedCount = statResult.get(GatherConstants.RESULT_FAIL);
                    }
                    if (statResult.containsKey(GatherConstants.RESULT_PORTION)) {
                        portionCount = statResult.get(GatherConstants.RESULT_PORTION);
                    }
                }
            }
            if (succeedCount == assetCount) {
                taskLog.setTaskState(GatherConstants.STATE_SUCCESS);
            } else if (succeedCount == 0 && portionCount == 0) {
                taskLog.setTaskState(GatherConstants.STATE_FAIL);
            } else {
                taskLog.setTaskState(GatherConstants.STATE_PORTION);
            }
            if (request.isCancel() && !Objects.equals(taskLog.getTaskState(), GatherConstants.STATE_SUCCESS)) {
                taskLog.setReason("采集任务取消");
            }
            if (log.isInfoEnabled()) {
                log.info(String.format("Gather end, assetCount:%s, fail:%s, portion:%s, succeed:%s", assetCount,
                        failedCount, portionCount, succeedCount));
                log.info(String.format("Size {%s} of devices supported by the gather template",
                        request.getGatherSize()));
                log.info(String.format("Task type:%s, plan:%s.", request.getTaskType(), request.getPlanName()));
            }
            taskLog.setAssetSize(request.getGatherSize());
            taskLog.setEnd(new Date());
            Map<String, Object> result = taskLog.getTaskResult();
            result.put(GatherConstants.SUCCEED_COUNT, succeedCount);
            result.put(GatherConstants.PORTION_COUNT, portionCount);
            result.put(GatherConstants.FAILED_COUNT, failedCount);
            gatherTaskLogRepository.save(taskLog);
            // 采集结束
            Map<String, Object> params = new HashMap<>();
            params.put(AmqpNotificationService.MSG_GATHER_JOB_FINISHED_CONTENT_LOGID, taskLog.getId());
            params.put(AmqpNotificationService.MSG_GATHER_JOB_FINISHED_CONTENT_ENDTIME, taskLog.getEnd());
            params.put(AmqpNotificationService.MSG_GATHER_JOB_FINISHED_CONTENT_STARTTIME, taskLog.getBegin());
            params.put(AmqpNotificationService.MSG_GATHER_JOB_FINISHED_CONTENT_ASSETIDS,
                    new ArrayList<>(request.getNoFailureAssets()));
            amqpNotificationService.sendNotification(AmqpNotificationService.MSG_GATHER_JOB_FINISHED, params);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Gather task request handle exception, plan:%s, taskType:%s.",
                        request.getPlanName(), request.getTaskType()), e);
            }
        } finally {
            // 清除较大的对象引用
            request.clearBigData();
        }
    }

    /**
     * 采集任务请求入采集队列
     *
     * @param request 采集任务请求
     */
    public void addQueue(GatherTaskRequest request) {
        taskQueue.add(request);
    }

    /**
     * 处理采集任务请求，初始化采集日志
     *
     * @param taskRequest 采集任务请求
     */
    public void doInternalSync(GatherTaskRequest taskRequest) {
        GatherTaskLogEs taskLog = taskRequest.getGatherTaskLogES();
        try {
            taskLog.setTaskState(GatherConstants.STATE_PROCESSING);
            taskLog.setBegin(new Date());
            checkParameterValidity(taskLog, taskRequest);
            if (taskRequest.isCancel()) {
                throw new GatherException("采集任务取消");
            }
            classifyGatherAssets(taskRequest);
            Set<String> sysTypeSet = taskRequest.getOption(GatherTaskRequest.EXTRA_SYSTYPE_SET);
            List<Asset> assetIds = taskRequest.getAssetList();
            if (log.isInfoEnabled()) {
                log.info(String.format("Gather plan:%s, taskType:%s, sysType's size:%s ,asset's size:%s.)",
                        taskRequest.getPlanName(), taskRequest.getTaskType(), sysTypeSet.size(), assetIds.size()));
            }
            if (assetIds.isEmpty()) {
                throw new GatherException("没有可检查的资产");
            }
            if (log.isDebugEnabled()) {
                log.debug("GatherTask begin inspect. log id = " + taskLog.getId());
            }
            gatherData(taskRequest);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Process gather task request exception, plan:%s, taskType:%s.",
                        taskLog.getPlanName(), taskLog.getTaskType()), e);
            }
            taskLog.setReason(e.getMessage());
            taskLog.setTaskState(GatherConstants.STATE_FAIL);
        }
        gatherTaskLogRepository.save(taskLog);
    }

    /**
     * 检查参数合法性（执行用户信息）
     *
     * @param taskLog     采集任务请求日志
     * @param taskRequest 采集任务请求
     */
    private void checkParameterValidity(GatherTaskLogEs taskLog, GatherTaskRequest taskRequest) {
        Long executorId = taskRequest.getExecutorId();
        if (null != executorId) {
            User executor = userRepository.findById(executorId).orElse(null);
            if (null == executor) {
                throw new GatherException(String.format("没有ID为%d的用户", executorId));
            }
            taskRequest.setExecutor(executor);
            taskLog.setExecutorId(executor.getId());
        }
    }

    /**
     * 根据IP的网段对待采集的资产分类
     *
     * @param request 采集任务请求
     */
    private void classifyGatherAssets(GatherTaskRequest request) {
        List<Asset> assets = request.getAssetList();
        if (CommUtils.isEmptyOfCollection(assets)) {
            return;
        }
        Map<String, List<Long>> assetMap = new HashMap<>();
        request.addOption(GatherTaskRequest.EXTRA_GATHER_ASSETS, assetMap);
        Set<String> sysTypeSet = new HashSet<>();
        request.addOption(GatherTaskRequest.EXTRA_SYSTYPE_SET, sysTypeSet);
        assets.forEach(asset -> {
            String assetIp = asset.getIp();
            int indexNum = assetIp.lastIndexOf(".");
            String subnet = "ipv6";
            if (indexNum != -1) {
                subnet = assetIp.substring(0, assetIp.lastIndexOf("."));
            }
            assetMap.computeIfAbsent(subnet, s -> new ArrayList<>());
            List<Long> sameSubnetAssets = assetMap.get(subnet);
            sameSubnetAssets.add(asset.getId());
            sysTypeSet.add(asset.getAssetSysType().getName());
        });

    }

    /**
     * 新建资产采集请求日志（采集线程根据未开始采集的日志进行选择采集）
     *
     * @param request 采集任务请求
     */
    private void gatherData(GatherTaskRequest request) {
        GatherTaskLogEs taskLog = request.getGatherTaskLogES();
        Map<String, List<Long>> assetMap = request.getOption(QUARTZ_DATA_MAP_ASSETS);
        int assetSize = request.getAssetList().size();
        if (log.isInfoEnabled()) {
            log.info(String.format("Start gather task, plan:%s, taskType:%s, assetSize:%d.", taskLog.getPlanName(),
                    taskLog.getTaskType(), assetSize));
        }
        recordRuntimeMemory();
        List<GatherAssetLogEs> assetLogList = new ArrayList<>();
        for (List<Long> assetIds : assetMap.values()) {
            if (request.isCancel()) {
                throw new GatherException("采集任务取消");
            }
            for (Long assetId : assetIds) {
                Asset asset = assetRepository.findById(assetId).orElse(null);
                if (null == asset) {
                    if (log.isInfoEnabled()) {
                        log.info(String.format("Asset %s has been deleted.", assetId));
                    }
                    continue;
                }
                GatherAssetLogEs assetLog = new GatherAssetLogEs();
                assetLog.setAssetId(assetId);
                assetLog.setAssetName(asset.getName());
                assetLog.setAssetIp(asset.getIp());
                assetLog.setGatherId(UUID.randomUUID().toString());
                assetLog.setTaskLogId(taskLog.getId());
                assetLog.setTaskType(taskLog.getTaskType());
                assetLog.setPlanId(taskLog.getPlanId());
                assetLog.setPlanName(taskLog.getPlanName());
                assetLog.setCreate(new Date().getTime());
                assetLogList.add(assetLog);
            }
            gatherAssetLogEsRepository.saveAll(assetLogList);
            assetLogList.clear();
            if (log.isDebugEnabled()) {
                log.info(String.format("The number of initialized assetLog is %d.", assetLogList.size()));
            }
        }
        taskLog.setTaskState(GatherConstants.STATE_CANCELABLE);
    }

    /**
     * 记录当前内存的使用情况
     */
    public void recordRuntimeMemory() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory() / 1024 / 1024;
        long total = runtime.totalMemory() / 1024 / 1024;
        long max = runtime.maxMemory() / 1024 / 1024;
        if (log.isInfoEnabled()) {
            log.info(String.format("Current memory usage:free %s, total %s, max %s.", free, total, max));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Long planId = (Long) jobDataMap.get(QUARTZ_DATA_MAP_PLANID);
        String planName = (String) jobDataMap.get(QUARTZ_DATA_MAP_PLANNAME);
        String taskType = (String) jobDataMap.get(QUARTZ_DATA_MAP_TASKTYPE);
        AtomicBoolean sendTask = (AtomicBoolean) jobDataMap.get("scanTask");
        List<Asset> gatherAssets = (List<Asset>) jobDataMap.get(QUARTZ_DATA_MAP_ASSETS);
        GatherTaskManager gatherTaskManager =
                (GatherTaskManager) context.getJobDetail().getJobDataMap().get(QUARTZ_DATA_MAP_MANAGER);
        GatherTaskRequest taskRequest = new GatherTaskRequest();
        taskRequest.setPlanId(planId);
        taskRequest.setPlanName(planName);
        taskRequest.setAssetList(gatherAssets);
        taskRequest.setTaskType(taskType);
        // 将定期执行的采集任务加入到执行队列
        gatherTaskManager.executeSubTask(taskRequest, true, sendTask);
    }

    /**
     * 更新采集计划的子任务列表
     *
     * @param request 采集子任务请求
     */
    public void addTaskRequest(GatherTaskRequest request) {
        if (null == request || null == request.getPlanId()) {
            return;
        }
        Long planId = request.getPlanId();
        synchronized (planTasks) {
            planTasks.computeIfAbsent(planId, s -> new CopyOnWriteArraySet<>());
            Set<GatherTaskRequest> tasks = planTasks.get(planId);
            if (null != tasks) {
                tasks.add(request);
            }
        }
        planGatherIdAndIp.computeIfAbsent(planId, s -> new HashMap<>());
    }

    /**
     * 获取采集计划是否在运行
     *
     * @param planId       采集计划ID
     * @param ignoreCancel 是否忽略任务取消状态
     * @return 采集任务是否在运行
     */
    public boolean isRunning(Long planId, boolean ignoreCancel) {
        boolean running = true;
        Set<GatherTaskRequest> taskRequests = planTasks.get(planId);
        if (!CommUtils.isEmptyOfCollection(taskRequests)) {
            if (!ignoreCancel) {
                running = !cancellingPlans.contains(planId);
            }
        } else {
            running = false;
        }
        return running;
    }

    /**
     * 根据采集计划ID判断采集计划是否正在取消中
     *
     * @param planId 采集计划ID
     * @return 计划是否正在取消中
     */
    public boolean isCancelling(Long planId) {
        return cancellingPlans.contains(planId);
    }

    /**
     * 指定类型的任务是否在运行,尚未取消完成的计划视为运行中
     *
     * @param planId   采集任务ID
     * @param taskType 任务类型
     * @return 是否再运行
     */
    public boolean isRunning(Long planId, String taskType) {
        if (null == planId || null == taskType) {
            return false;
        }
        synchronized (planTasks) {
            Set<GatherTaskRequest> tasks = planTasks.get(planId);
            if (CommUtils.isEmptyOfCollection(tasks)) {
                return false;
            }
            for (GatherTaskRequest request : tasks) {
                if (taskType.equals(request.getTaskType())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 指定类型的采集任务是否在运行
     *
     * @param planId 采集计划ID
     * @param taskId 采集任务ID
     * @return 是否在运行
     */
    public boolean taskIsRunning(Long planId, String taskId) {
        if (null == planId || null == taskId) {
            return false;
        }
        synchronized (planTasks) {
            Set<GatherTaskRequest> taskRequests = planTasks.get(planId);
            if (CommUtils.isEmptyOfCollection(taskRequests)) {
                return false;
            }
            for (GatherTaskRequest taskRequest : taskRequests) {
                GatherTaskLogEs taskLog = taskRequest.getGatherTaskLogES();
                if (taskId.equals(taskLog.getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 取消采集任务
     *
     * @param planId 计划ID
     */
    boolean cancelGatherTask(Long planId) {
        Set<GatherTaskRequest> cancelTasks = new HashSet<>();
        synchronized (cancellingPlans) {
            if (cancellingPlans.contains(planId)) {
                if (log.isInfoEnabled()) {
                    log.info(String.format("Gather plan %s is cancelling.", planId));
                }
                return false;
            }
            Set<GatherTaskRequest> tmpTasks = planTasks.get(planId);
            if (CommUtils.isEmptyOfCollection(tmpTasks)) {
                return true;
            }
            cancelTasks.addAll(tmpTasks);
            cancellingPlans.add(planId);
            gatherPlanService.updatePlanState(planId, GatherConstants.STATE_CANCELLING);
            for (GatherTaskRequest cancelTask : cancelTasks) {
                GatherTaskLogEs taskLog = cancelTask.getGatherTaskLogES();
                cancelTask.setCancel(true);
                taskLog.setTaskState(GatherConstants.STATE_CANCELLING);
                gatherTaskLogRepository.save(taskLog);
            }
        }
        for (GatherTaskRequest cancelTask : cancelTasks) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Gather task Gather exception cancel, plan:%s, task type:%s, future size:%d.",
                        cancelTask.getPlanName(), cancelTask.getTaskType(), cancelTask.getFutureList().size()));
            }
            cancelTask.cancelFuture();
        }
        updateAssetLogTaskCancel(planId, cancelTasks);
        cancellingPlans.remove(planId);
        return true;
    }

    /**
     * 任务取消时更新采集任务日志和资产采集日志
     *
     * @param planId      采集计划ID
     * @param cancelTasks 取消的采集任务请求列表
     */
    private void updateAssetLogTaskCancel(Long planId, Set<GatherTaskRequest> cancelTasks) {
        Map<String, GatherTaskRequest> cancelTaskMap = new HashMap<>();
        try {
            for (GatherTaskRequest cancelTask : cancelTasks) {
                cancelTaskMap.put(cancelTask.getGatherTaskLogES().getId(), cancelTask);
            }
            List<String> gatherIds = gatherAssetLogEsService.getUnCompletedGatherId(planId, null);
            if (!CommUtils.isEmptyOfCollection(gatherIds)) {
                Date uTime = new Date();
                for (String gatherId : gatherIds) {
                    GatherAssetLogEs assetLog = gatherAssetLogEsRepository.findByGatherId(gatherId);
                    if (null == assetLog) {
                        continue;
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("GatherId %s of asset %s is canceling...", gatherId,
                                assetLog.getAssetName()));
                    }
                    if (null == assetLog.getEnd()) {
                        GatherTaskRequest taskRequest = cancelTaskMap.get(assetLog.getTaskLogId());
                        if (null != taskRequest) {
                            if (taskRequest.addCompletedGatherId(assetLog.getGatherId())) {
                                try {
                                    statisticGatherResult(assetLog, taskRequest);
                                } catch (Exception e) {
                                    if (log.isErrorEnabled()) {
                                        log.error(String.format("Statistic gather result exception, " +
                                                "gatherId:%s, asset:%s", gatherId, assetLog.getAssetName()), e);
                                    }
                                } finally {
                                    gatherEndCommonProcess(taskRequest, assetLog);
                                }
                            }
                        } else {
                            if (null == assetLog.getBegin()) {
                                assetLog.setBegin(uTime);
                            }
                            assetLog.setEnd(uTime);
                            assetLog.setResult(GatherConstants.RESULT_FAIL);
                            assetLog.setState(GatherConstants.STATE_END);
                            gatherAssetLogEsRepository.save(assetLog);
                            notifyLock();
                        }
                    }
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Asset gather has been canceled,gatherId: %s, assetName: %s.", gatherId,
                                assetLog.getAssetName()));
                    }
                }
            }
            // 此处for不使用cancelTasks，taskRequestHandle会删除集合中实例，避免ConcurrentModificationException
            synchronized (planTasks) {
                for (GatherTaskRequest taskRequest : cancelTasks) {
                    taskRequestHandle(taskRequest);
                    gatherPlanService.judgeAndUpdatePlanStatus(taskRequest.getPlanId(), false);
                }
                planTasks.remove(planId);
                planGatherIdAndIp.remove(planId);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Update assetLog exception and taskLog when gather plan %s.", planId), e);
            }
        }
    }

    /**
     * 记录采集任务下，单个资产的某次采集任务的变更情况
     *
     * @param planId   采集计划ID
     * @param assetLog 资产采集日志
     * @param add      新增或删除采集ID（true：新增，false：删除）
     * @return 返回采集任务添加或删除结果
     */
    private boolean updateAssetGatherTask(Long planId, GatherAssetLogEs assetLog, boolean add) {
        synchronized (planGatherIdAndIp) {
            Map<String, String> tasks = planGatherIdAndIp.get(planId);
            if (null == tasks) {
                return false;
            }
            if (add) {
                if (tasks.containsKey(assetLog.getGatherId())) {
                    return false;
                }
                tasks.put(assetLog.getGatherId(), assetLog.getAssetIp());
            } else {
                tasks.remove(assetLog.getGatherId());
            }
        }
        return true;
    }

    /**
     * 获取当前正在运行的采集任务请求
     *
     * @param planId   采集计划ID
     * @param taskType 任务类型
     * @return 采集任务请求
     */
    private GatherTaskRequest getTaskRequest(Long planId, String taskType) {
        synchronized (planTasks) {
            Set<GatherTaskRequest> taskRequests = planTasks.get(planId);
            if (CommUtils.isEmptyOfCollection(taskRequests)) {
                if (log.isInfoEnabled()) {
                    log.info("Gather plan is been cancel, plan:" + planId);
                }
                return null;
            }
            for (GatherTaskRequest req : taskRequests) {
                if (req.getTaskType().equals(taskType)) {
                    return req;
                }
            }
        }
        return null;
    }

    /**
     * 采集结束后的通用处理流程
     *
     * @param taskRequest 任务请求
     * @param assetLog    采集日志
     */
    private void gatherEndCommonProcess(GatherTaskRequest taskRequest, GatherAssetLogEs assetLog) {
        if (null == taskRequest) {
            taskRequest = getTaskRequest(assetLog.getPlanId(), assetLog.getTaskType());
        }
        if (null != taskRequest) {
            // 处理因异常导致已进入线程池排队的采集任务未删除的情况
            taskRequest.getQueuedTask().remove(assetLog.getGatherId());
            if (!GatherConstants.RESULT_FAIL.equals(assetLog.getResult())) {
                taskRequest.getNoFailureAssets().add(assetLog.getAssetId());
            }
            updateAssetGatherTask(taskRequest.getPlanId(), assetLog, false);
            taskRequest.getGatherCount().incrementAndGet();
        }
        agentGatherEndIds.remove(assetLog.getGatherId());
        notifyLock();
    }

    /**
     * 保存采集结果
     *
     * @param taskRequest 任务请求
     * @param assetLog    采集日志
     * @param result      采集结果
     * @param errMsg      错误信息
     */
    private void saveGatherResult(GatherTaskRequest taskRequest, GatherAssetLogEs assetLog, Integer result,
                                  String errMsg) {
        try {
            if (log.isDebugEnabled()) {
                log.info(String.format("Gather result:%d, errMsg:%s.", result, null == errMsg ? "" : errMsg));
            }
            if (!GatherConstants.RESULT_SUCCESS.equals(result) && null != errMsg) {
                assetLog.setReason(errMsg);
            }
            Date time = new Date();
            if (null == assetLog.getBegin()) {
                // 未开始的任务，开始时间和结束时间设置相同
                assetLog.setBegin(time);
            }
            assetLog.setEnd(time);
            assetLog.setResult(result);
            assetLog.setState(GatherConstants.STATE_END);
            try {
                gatherAssetLogEsRepository.save(assetLog);
                if (GatherConstants.RESULT_FAIL.equals(result)) {
                    taskRequest.getFailedCount().incrementAndGet();
                } else if (GatherConstants.RESULT_PORTION.equals(result)) {
                    taskRequest.getPortionCount().incrementAndGet();
                } else if (GatherConstants.RESULT_SUCCESS.equals(result)) {
                    taskRequest.getSucceedCount().incrementAndGet();
                }
            } catch (Exception e) {
                if (e instanceof MapperParsingException) {
                    if (log.isErrorEnabled()) {
                        log.error(String.format("Save gather asset log exception, gatherId:%s.",
                                assetLog.getGatherId()), e);
                    }
                    assetLog.getItemlogs().clear();
                }
                assetLog.setResult(GatherConstants.RESULT_FAIL);
                assetLog.setReason("采集日志保存异常");
                gatherAssetLogEsRepository.save(assetLog);
                taskRequest.getFailedCount().incrementAndGet();
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("GatherId:%s, cancel:%s, result:%s", assetLog.getGatherId(),
                        taskRequest.isCancel(), result));
            }
            if (!taskRequest.isCancel()) {
                // 任务取消不进行解析
                gatherEsService.saveOrUpdate(assetLog.getGatherId());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Save gather result exception, gatherId:%s, asset:%s", assetLog.getGatherId(),
                        assetLog.getAssetName()), e);
            }
        }
    }

    /**
     * 统计采集结果
     *
     * @param assetLog    资产采集日志
     * @param taskRequest 任务采集请求
     */
    private void statisticGatherResult(GatherAssetLogEs assetLog, GatherTaskRequest taskRequest) {
        // 失败原因提取
        try {
            String interruptReason = addInterruptItemLogs(assetLog, taskRequest.isCancel());
            Integer failNum = assetLog.getSize();
            List<GatherItemLog> itemLogs = assetLog.getItemlogs();
            if (!CommUtils.isEmptyOfCollection(itemLogs)) {
                for (GatherItemLog itemLog : itemLogs) {
                    if (GatherConstants.RESULT_SUCCESS.equals(itemLog.getResult())) {
                        failNum--;
                    }
                }
            }
            assetLog.setAbnormalSize(failNum);
            if (taskRequest.isCancel()) {
                // 任务取消，设置失败
                saveGatherResult(taskRequest, assetLog, GatherConstants.RESULT_FAIL, interruptReason);
            } else if (failNum == 0) {
                // 成功
                saveGatherResult(taskRequest, assetLog, GatherConstants.RESULT_SUCCESS, interruptReason);
            } else if (Objects.equals(failNum, assetLog.getSize())) {
                // 失败
                saveGatherResult(taskRequest, assetLog, GatherConstants.RESULT_FAIL, interruptReason);
            } else {
                // 部分成功
                saveGatherResult(taskRequest, assetLog, GatherConstants.RESULT_PORTION, interruptReason);
            }
            // 清除 itemLogEntity
            gatherItemLogEsService.deleteItemLogByGatherId(assetLog.getGatherId());
            if (log.isDebugEnabled()) {
                log.debug("GatherTaskLog end run task.taskResult=" + assetLog.getResult());
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Statistics gather result exception, gatherId:%s, asset:%s",
                        assetLog.getGatherId(), assetLog.getAssetName()), e);
            }
        }
    }

    /**
     * 添加因采集中断而未采集的采集项日志
     *
     * @param assetLog 采集日志
     * @param cancel   是否是任务取消
     * @return 中断的原因
     */
    private String addInterruptItemLogs(GatherAssetLogEs assetLog, boolean cancel) {
        String reason = null;
        List<GatherItemLog> itemLogs = assetLog.getItemlogs();
        if (log.isInfoEnabled()) {
            log.info(String.format("Item logs size:%s, gather item size:%s", itemLogs.size(), assetLog.getSize()));
        }
        if (cancel) {
            if (log.isInfoEnabled()) {
                log.info(String.format("Task cancel, asset:%s, gatherId:%s.", assetLog.getAssetName(),
                        assetLog.getGatherId()));
            }
            reason = "Collection task canceled";
        } else if (null == assetLog.getResult() && 0 != assetLog.getSize() && itemLogs.size() != assetLog.getSize()) {
            // 采集超时
            if (log.isInfoEnabled()) {
                log.info(String.format("Gather timeout, asset:%s, gatherId:%s.", assetLog.getAssetName(),
                        assetLog.getGatherId()));
            }
            reason = "Acquisition timeout";
        }
        if (null != reason) {
            // 若已采集的项数为0，即全部失败，不再为每个采集项设置失败原因
            if (!CommUtils.isEmptyOfCollection(itemLogs)) {
                List<String> itemKeys = new ArrayList<>(assetLog.getItemKeys());
                itemLogs.forEach(itemLog -> itemKeys.remove(itemLog.getItemKey()));
                for (String key : itemKeys) {
                    GatherItemLog item = new GatherItemLog();
                    item.setItemKey(key);
                    item.setOutput(new HashMap<>());
                    item.setElite(new HashMap<>());
                    item.setResult(GatherConstants.RESULT_FAIL);
                    item.setReason(reason);
                    itemLogs.add(item);
                }
            }
        }
        return reason;
    }

    /**
     * TODO zhaoff
     * agent采集项日志收集器
     *
     * @author zhaoff
     */
    private class ItemLogCollector implements Runnable {

        /**
         * 采集ID
         */
        private final String gatherId;

        /**
         * 默认构造方法
         *
         * @param gatherId 采集ID
         */
        public ItemLogCollector(String gatherId) {
            this.gatherId = gatherId;
        }

        @Override
        public void run() {
            GatherAssetLogEs assetLog = gatherAssetLogEsRepository.findByGatherId(gatherId);
            if (log.isInfoEnabled()) {
                log.info(String.format("Gather over, gatherId:%s, taskType:%s, asset:%s", gatherId,
                        assetLog.getTaskType(), assetLog.getAssetName()));
            }
            List<GatherItemLogEs> itemLogEsList = gatherItemLogEsRepository.findByGatherId(gatherId);
            if (null != itemLogEsList) {
                itemLogEsList.forEach(entity -> assetLog.getItemlogs().add(new GatherItemLog(entity)));
            }
            GatherTaskRequest taskRequest = getTaskRequest(assetLog.getPlanId(), assetLog.getTaskType());
            if (null != taskRequest && taskRequest.addCompletedGatherId(assetLog.getGatherId())) {
                try {
                    statisticGatherResult(assetLog, taskRequest);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(String.format("Statistic gather result exception, gatherId:%s, asset:%s", gatherId,
                                assetLog.getAssetName()), e);
                    }
                } finally {
                    gatherEndCommonProcess(taskRequest, assetLog);
                }
            }
        }
    }

    /**
     * 采集线程
     *
     * @author zhaoff
     */
    private class GatherSubWork implements Runnable {

        /**
         * 采集任务请求
         */
        GatherTaskRequest taskRequest;
        /**
         * 资产采集请求
         */
        GatherAssetRequest assetRequest;
        /**
         * 资产采集日志
         */
        GatherAssetLogEs assetLogES;
        /**
         * 采集资产ID
         */
        private Long assetId;

        /**
         * 构造方法
         *
         * @param assetLogES  资产采集日志
         * @param taskRequest 采集任务请求
         */
        public GatherSubWork(GatherAssetLogEs assetLogES, GatherTaskRequest taskRequest) {
            this.assetLogES = assetLogES;
            this.taskRequest = taskRequest;
        }

        @Override
        public void run() {
            if (null == this.assetLogES || null == this.taskRequest) {
                return;
            }
            // 执行采集前查询计划是否被停止，若计划停止则不执行采集
            GatherAssetLogEs newAssetLog = gatherAssetLogEsRepository.findById(assetLogES.getId()).orElse(null);
            if (null == newAssetLog || GatherConstants.STATE_STOP.equals(newAssetLog.getState())) {
                return;
            }
            if (!updateAssetGatherTask(assetLogES.getPlanId(), assetLogES, true)) {
                if (log.isWarnEnabled()) {
                    log.warn(String.format("Gather work is running for gatherId %s.", assetLogES.getGatherId()));
                }
                return;
            }
            this.assetId = assetLogES.getAssetId();
            assetLogES.setBegin(new Date());
            assetLogES.setState(GatherConstants.STATE_RUNNING);
            Asset asset = assetRepository.findById(assetId).orElse(null);
            if (null == asset) {
                if (log.isWarnEnabled()) {
                    log.warn(String.format("Gather asset with ID %s has been deleted", assetId));
                }
                throw new GatherException("Gather asset has been deleted");
            }
            String assetIP;
            try {
                assetIP = asset.getIp();
                if (StringUtils.isEmpty(assetIP)) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("The ip of gather asset named %s is null.", asset.getName()));
                    }
                    return;
                }
                this.assetLogES.setAssetType(asset.getAssetType().getId());
                this.assetLogES.setAssetName(asset.getName());
                this.assetLogES.setAssetIp(assetIP);
                // 用作唯一标识
                this.assetLogES.setFlag(taskRequest.getFlag());
                gatherAssetLogEsRepository.save(this.assetLogES);
                if (taskRequest.isCancel()) {
                    throw new InterruptedException("Gather task cancel");
                }
                if (log.isDebugEnabled()) {
                    log.info(String.format("Gather plan={name:%s, type:%s}, asset={name:%s, ip:%s}, gatherId={%s}.",
                            assetLogES.getPlanName(), assetLogES.getTaskType(), assetLogES.getAssetName(),
                            assetLogES.getAssetIp(), assetLogES.getGatherId()));
                }
                this.assetRequest = genGatherAssetRequest(taskRequest, asset, this.assetLogES);
                // 预采集
                try {
                    preInspect(assetRequest);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error(String.format("PreGather exception, mainCategory=%s, category=%s.",
                                assetRequest.getMainCategory(), assetRequest.getCategory()));
                    }
                    throw e;
                }

                // 此次任务采集的数量
                this.assetLogES.setSize(this.assetRequest.getCmds().size());
                this.assetLogES.setItemKeys(this.assetRequest.getCmds());

                // 获取资产版本信息
                GatherAssetRequest preAssetRequest = assetRequest.getPreRequest();
                if (null != preAssetRequest) {
                    Map<String, Object> preVars = assetRequest.getRequireVars();
                    for (String key : preVars.keySet()) {
                        if (key.contains("version")) {
                            assetLogES.setVersion(String.valueOf(preVars.get(key)));
                        }
                    }
                }
                gatherAssetLogEsRepository.save(assetLogES);
                // 正式采集
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Start gather, mainCategory=%s, category=%s.",
                            assetRequest.getMainCategory(), assetRequest.getCategory()));
                }

                gatherProviderManager.execute(assetRequest);
                Asset nowAsset = assetRepository.findById(assetId).orElse(null);
                if (null == nowAsset || !nowAsset.getIp().equals(assetIP)) {
                    assetRequest.setSuccess(false);
                    assetRequest.setErrorMsg("The asset does not exist, or the asset IP has been modified");
                }
                if (!assetRequest.isSuccess()) {
                    throw new GatherException("Gather fail");
                }
                if (log.isInfoEnabled()) {
                    log.info(String.format("Gathered with non-agent, asset={name:%s, ip:%s}, gatherId={%s}.",
                            asset.getName(), asset.getIp(), assetLogES.getGatherId()));
                }
                if (taskRequest.addCompletedGatherId(assetLogES.getGatherId())) {
                    cmdRequest2ItemLog();
                    statisticGatherResult(assetLogES, taskRequest);
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error(String.format("Gather exception, asset={name:%s, ip:%s}, gatherId={%s}.",
                            assetLogES.getAssetName(), assetLogES.getAssetIp(), assetLogES.getGatherId()), e);
                }
                if (taskRequest.addCompletedGatherId(assetLogES.getGatherId())) {
                    processGatherException(e);
                }
            } finally {
                if (null != assetLogES.getEnd()) {
                    gatherEndCommonProcess(taskRequest, assetLogES);
                }
                if (null != assetRequest) {
                    assetRequest.setAsset(null);
                    assetRequest.getResults().clear();
                    assetRequest.clearOption();
                    assetRequest = null;
                }
            }
        }

        /**
         * 处理采集异常
         *
         * @param e 采集异常
         */
        private void processGatherException(Exception e) {
            String errMsg = null;
            if (null != assetRequest) {
                errMsg = this.assetRequest.getErrorMsg();
            }
            if (null == errMsg) {
                errMsg = e.getMessage();
            }
            String interruptReason = null;
            boolean cancel = (e instanceof InterruptedException || errMsg.contains("wait interrupt")
                    || errMsg.contains("interrupted"));
            if (cancel) {
                interruptReason = addInterruptItemLogs(assetLogES, true);
            }
            if (null != interruptReason) {
                errMsg = interruptReason;
            }
            saveGatherResult(taskRequest, assetLogES, GatherConstants.RESULT_FAIL, errMsg);
        }

        /**
         * 将采集结果转换为 itemLog
         */
        private void cmdRequest2ItemLog() {
            Set<String> reasonSet = new HashSet<>();
            for (GatherCmdRequest cmdRequest : assetRequest.getCmdRequest()) {
                if (!cmdRequest.isSuccess()) {
                    String errorMsg = cmdRequest.getErrorMsg();
                    if (errorMsg.endsWith("\n")) {
                        errorMsg = errorMsg.substring(0, errorMsg.length() - 2);
                    }
                    reasonSet.add(errorMsg);
                }
                GatherItemLog item = new GatherItemLog();
                item.setItemKey(cmdRequest.getKey());
                item.setElite(cmdRequest.getVars());
                item.setOutput(cmdRequest.getOutputs());
                // TODO zhaoff 结果过大时，存文件
                item.setResult(cmdRequest.isSuccess() ? GatherConstants.RESULT_SUCCESS : GatherConstants.RESULT_FAIL);
                item.setReason(cmdRequest.getErrorMsg());
                assetLogES.addItemLog(item);
            }
            if (!CommUtils.isEmptyOfCollection(reasonSet)) {
                assetLogES.setReason(StringUtils.join(reasonSet, "\n"));
            }
        }

        /**
         * 生成资产采集请求
         *
         * @param taskRequest 采集请求
         * @param asset       采集资产
         * @param assetLog    采集日志
         * @return 资产采集请求
         * @throws GatherException 采集异常
         */
        private GatherAssetRequest genGatherAssetRequest(GatherTaskRequest taskRequest, Asset asset,
                                                         GatherAssetLogEs assetLog) throws GatherException {
            // 资产一级分类
            String type = asset.getAssetType().getId().toString();
            // 资产二级分类
            String sysType = asset.getAssetSysType().getId().toString();
            List<GatherDetail> details = gatherCenter.getIndicatorsByMainAndCate(type, sysType);
            GatherXmlCategoryBean category = gatherCenter.getCategory(type, sysType);
            if (CommUtils.isEmptyOfCollection(details) || details.size() == 1 || null == category) {
                if (log.isDebugEnabled()) {
                    log.debug("Asset type is not supported. type=" + type + "-" + sysType);
                }
                throw new GatherException("There is no matching collection item for this asset");
            }
            GatherAssetRequest assetRequest = new GatherAssetRequest(type, sysType);
            assetRequest.setTaskType(assetLog.getTaskType());
            assetRequest.setAsset(asset);
            assetRequest.setAssetLog(assetLog);
            assetRequest.setTto(getGatherTaskTimeout(taskRequest.getTaskType()));
            for (GatherDetail detail : details) {
                if (detail.itemKey().startsWith("_prerequisite")) {
                    continue;
                }
                // 采集类型过滤
                Integer collectType = detail.getCollectType();
                if (null == collectType) {
                    collectType = GatherConstants.TYPE_SELDOM_ITEM;
                }
                if (!collectType.toString().equals(assetLog.getTaskType())) {
                    continue;
                }
                GatherCmdRequest cmdRequest = new GatherCmdRequest(detail.itemKey());
                assetRequest.addCmdRequest(cmdRequest);
            }
            taskRequest.getGatherAssets().add(asset.getId());
            return assetRequest;
        }

        /**
         * 预采集
         *
         * @param gatherAssetRequest 资产采集请求
         * @throws GatherException 采集异常
         */
        @SuppressWarnings("unchecked")
        private void preInspect(GatherAssetRequest gatherAssetRequest) throws GatherException {
            Asset asset = gatherAssetRequest.getAsset();
            String assetType = asset.getAssetType().getId().toString();
            String assetSysType = asset.getAssetSysType().getId().toString();
            Set<String> indicators = new HashSet<>(gatherAssetRequest.getCmds());
            GatherAssetRequest preAssetRequest = null;
            for (String key : indicators) {
                List<String> preKeys = gatherCenter.getPreKeys(assetType, assetSysType, key);
                for (String preKey : preKeys) {
                    GatherDetail detail = gatherCenter.getGatherDetail(assetType, assetSysType, preKey);
                    if (null == detail) {
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("Failed to get detail.mainCategory:<%s>,category:<%s>,preKey:<%s>",
                                    assetType, assetSysType, preKey));
                        }
                        continue;
                    }
                    preAssetRequest = new GatherAssetRequest(assetType, assetSysType);
                    preAssetRequest.setAsset(asset);
                    GatherCmdRequest preCmdRequest = new GatherCmdRequest(preKey);
                    preAssetRequest.addCmdRequest(preCmdRequest);
                }
            }
            if (null == preAssetRequest || CommUtils.isEmptyOfCollection(preAssetRequest.getCmds())) {
                throw new GatherException("The content of the collection item is empty");
            }
            gatherAssetRequest.setPreRequest(preAssetRequest);
            gatherProviderManager.execute(preAssetRequest);
            if (!preAssetRequest.isSuccess()) {
                String errMgs = preAssetRequest.getErrorMsg();
                throw new GatherException(errMgs);
            }
            Map<String, Object> requireVars = new HashMap<>();
            gatherAssetRequest.setRequireVars(requireVars);

            for (String key : indicators) {
                GatherDetail detail = gatherCenter.getGatherDetail(assetType, assetSysType, key);
                if (null == detail) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Failed to get gather item for %s, %s, %s",
                                assetType, assetSysType, key));
                    }
                    continue;
                }
                String ifParam = detail.getIfParam();
                List<String> preKeys = gatherCenter.getPreKeys(assetType, assetSysType, key);
                if (CommUtils.isEmptyOfCollection(preKeys) || null == ifParam) {
                    continue;
                }
                Map<String, Object> preVars = new HashMap<>();
                List<GatherCmdRequest> preCmdReqs = new ArrayList<>();
                for (String preKey : preKeys) {
                    GatherCmdRequest res = preAssetRequest.getCmdRequest(preKey);
                    if (null != res) {
                        preCmdReqs.add(res);
                        if (null != res.getVars() && res.getVars().size() > 0) {
                            preVars.putAll(res.getVars());
                        } else {
                            throw new GatherException(res.getErrorMsg());
                        }
                    }
                }
                // 条件过滤
                if (!CommUtils.isEmptyOfMap(preVars)) {
                    try {
                        Boolean nextOk = cmdOutputParser.evaluate(ifParam, preVars, Boolean.class);
                        if (!Objects.equals(nextOk, Boolean.TRUE)) {
                            gatherAssetRequest.removeCmdRequest(key);
                        }
                        requireVars.putAll(preVars);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("CmdOutputParser evaluate failed", e);
                        }
                        gatherAssetRequest.removeCmdRequest(key);
                        throw e;
                    }
                } else {
                    if (log.isWarnEnabled()) {
                        if (!CommUtils.isEmptyOfCollection(preCmdReqs)) {
                            for (GatherCmdRequest res : preCmdReqs) {
                                log.warn("Invalid vars.key:" + key + ",err msg:" + res.getErrorMsg() + ",output:"
                                        + res.getOutputs());
                            }
                        } else {
                            log.warn("Invalid vars.key:" + key);
                        }
                    }
                    gatherAssetRequest.removeCmdRequest(key);
                }
            }
            // 不支持的版本
            if (preAssetRequest.isSuccess() && CommUtils.isEmptyOfCollection(gatherAssetRequest.getCmdRequest())) {
                throw new GatherException("The asset version is not supported");
            }
        }

    }

}
