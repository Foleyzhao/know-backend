package com.cumulus.modules.business.gather.request;


import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.entity.es.GatherTaskLogEs;
import com.cumulus.modules.system.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 采集任务请求
 *
 * @author zhaoff
 */
@Getter
@Setter
public class GatherTaskRequest {

    /**
     * 额外配置信息-采集资产
     */
    public static final String EXTRA_GATHER_ASSETS = "gatherAssets";

    /**
     * 额外配置信息-采集系统类型
     */
    public static final String EXTRA_SYSTYPE_SET = "sysTypeSet";

    /**
     * 任务ID
     */
    private Long planId;

    /**
     * 任务名称
     */
    private String planName;

    /**
     * 执行用户ID
     */
    private Long executorId;

    /**
     * 标识同一个任务
     */
    private Long flag;

    /**
     * 执行用户
     */
    private User executor;

    /**
     * 任务类型（实时、耗时、不常变化）
     */
    private String taskType;

    /**
     * 采集资产ID列表
     */
    private List<Asset> assetList;

    /**
     * 扫描任务运行的future
     */
    private List<Future<?>> futureList = new CopyOnWriteArrayList<>();

    /**
     * 采集任务日志
     */
    private GatherTaskLogEs gatherTaskLogES;

    /**
     * 支持数据采集的资产
     */
    private CopyOnWriteArraySet<Long> gatherAssets;

    /**
     * 有采集结果的资产，包括部分成功和成功
     */
    private CopyOnWriteArraySet<Long> noFailureAssets;

    /**
     * 有采集结果的资产，包括部分成功和成功
     */
    private CopyOnWriteArraySet<String> completedGatherIds = new CopyOnWriteArraySet<>();

    /**
     * 采集失败计数
     */
    private AtomicInteger failedCount = new AtomicInteger(0);

    /**
     * 采集成功计数
     */
    private AtomicInteger succeedCount = new AtomicInteger(0);

    /**
     * 采集部分成功计数
     */
    private AtomicInteger portionCount = new AtomicInteger(0);

    /**
     * 资产采集计数
     */
    private AtomicInteger gatherCount = new AtomicInteger(0);

    /**
     * 额外配置信息
     * {
     * ”sysTypeSet“:Set<String>
     * ”gatherAssets“：Map<String, List<Long>>
     * }
     **/
    private Map<String, Object> extra = new HashMap<>();

    /**
     * 执行周期信息
     * {
     * "frequently":{  --实时
     * "interval":2,
     * "type":"day/month",
     * "startAt":"2022-01-01 00:00:00"
     * },
     * "stationary":{  --耗时
     * "interval":2,
     * "type":"hour",
     * "startAt":"2022-01-01 00:00:00"
     * },
     * "stationary":{  --不常变化
     * "interval":5,
     * "type":"minute",
     * "startAt":"2022-01-01 00:00:00"
     * }
     * type：day month hour minute once
     * }
     */
    private Map<String, Object> period = new HashMap<>();

    /**
     * 任务取消标记
     */
    private volatile boolean cancel = false;

    /**
     * 已加入线程池的采集任务的采集ID
     */
    private Set<String> queuedTask = ConcurrentHashMap.newKeySet();

    /**
     * 默认构造方法
     */
    public GatherTaskRequest() {
        noFailureAssets = new CopyOnWriteArraySet<>();
        gatherAssets = new CopyOnWriteArraySet<>();
        completedGatherIds = new CopyOnWriteArraySet<>();
    }

    /**
     * 构造方法
     *
     * @param planId    采集计划ID
     * @param planName  计划名称
     * @param execUser  执行用户
     * @param taskType  任务类型
     * @param assetList 采集资产列表
     * @param period    采集周期
     */
    public GatherTaskRequest(Long planId, String planName, User execUser, String taskType, List<Asset> assetList,
                             Map<String, Object> period) {
        this.planId = planId;
        this.planName = planName;
        this.executor = execUser;
        this.taskType = taskType;
        this.assetList = assetList;
        this.period = period;
        noFailureAssets = new CopyOnWriteArraySet<>();
        gatherAssets = new CopyOnWriteArraySet<>();
    }

    /**
     * 添加执行的任务
     *
     * @param future 执行任务
     */
    public void addFuture(Future<?> future) {
        futureList.add(future);
    }

    /**
     * 取消本次计划所有执行的任务
     */
    public void cancelFuture() {
        for (Future<?> future : futureList) {
            if (future.isCancelled() || future.isDone()) {
                continue;
            }
            future.cancel(true);
        }
    }

    /**
     * 返回采集的资产数量
     *
     * @return 资产数量
     */
    public Integer getGatherSize() {
        return gatherAssets.size();
    }

    /**
     * 判断采集任务是否执行完毕
     *
     * @return 结果
     */
    public boolean overCheck() {
        return assetList.size() == gatherCount.get();
    }

    /**
     * 从额外信息中获取参数
     *
     * @param key 参数名称
     * @param <T> 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getOption(String key) {
        Object obj = extra.get(key);
        if (null == obj) {
            return null;
        }
        return (T) obj;
    }

    /**
     * 向额外信息中中添加参数
     *
     * @param key   参数名称
     * @param value 参数值
     */
    public void addOption(String key, Object value) {
        extra.put(key, value);
    }

    /**
     * 清除较大的对象引用
     */
    public void clearBigData() {
        extra.clear();
        noFailureAssets.clear();
    }

    /**
     * 添加已完成采集ID
     *
     * @param gatherId 采集ID
     * @return 是否添加成功
     */
    public boolean addCompletedGatherId(String gatherId) {
        boolean result = true;
        synchronized (completedGatherIds) {
            if (completedGatherIds.contains(gatherId)) {
                result = false;
            } else {
                completedGatherIds.add(gatherId);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        GatherTaskRequest request = (GatherTaskRequest) o;
        return planId.equals(request.planId) && taskType.equals(request.taskType);
    }

}
