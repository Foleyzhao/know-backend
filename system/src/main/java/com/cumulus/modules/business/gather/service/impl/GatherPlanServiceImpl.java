package com.cumulus.modules.business.gather.service.impl;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.transaction.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.dto.GatherTaskDto;
import com.cumulus.modules.business.gather.dto.GatherTaskQueryCriteria;
import com.cumulus.modules.business.gather.entity.es.GatherResultEs;
import com.cumulus.modules.business.gather.entity.mysql.GatherPeriod;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import com.cumulus.modules.business.gather.mapper.GatherTaskMapper;
import com.cumulus.modules.business.gather.repository.GatherPeriodRepository;
import com.cumulus.modules.business.gather.repository.GatherPlanRepository;
import com.cumulus.modules.business.gather.service.GatherPlanService;
import com.cumulus.modules.business.gather.service.gather.GatherTaskManager;
import com.cumulus.modules.business.mapstruct.AssetMapper;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.utils.CronUtil;
import com.cumulus.modules.business.utils.TaskScheduleModel;
import com.cumulus.modules.business.vulnerability.entity.ScanPlan;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * 采集任务服务实现类
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class GatherPlanServiceImpl implements GatherPlanService {

    /**
     * 采集任务传输对象与采集任务实体的映射
     */
    @Resource
    private GatherTaskMapper mapper;

    /**
     * 资产任务传输对象与资产实体的映射
     */
    @Resource
    private AssetMapper assetMapper;

    /**
     * 采集任务数据访问接口
     */
    @Resource
    private GatherPlanRepository gatherPlanRepository;

    /**
     * 采集周期数据访问接口
     */
    @Resource
    private GatherPeriodRepository gatherPeriodRepository;

    /**
     * 采集任务管理类
     */
    @Lazy
    @Resource
    private GatherTaskManager gatherTaskManager;

    /**
     * 资产数据访问接口
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * 部门数据访问接口
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * ES模板
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * 查询采集任务
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 采集任务列表
     */
    @Override
    public Object queryAll(GatherTaskQueryCriteria criteria, Pageable pageable) {
        Page<GatherPlan> gatherTaskPage = gatherPlanRepository.findAll((root, query, criteriaBuilder) -> {
            query.distinct(true);
            return QueryUtils.getPredicate(root, criteria, criteriaBuilder);
        }, pageable);
        return gatherTaskPage.map(mapper::toDto);
    }

    /**
     * 新增采集任务
     *
     * @param gatherTaskDto 采集任务传输对象
     */
    @Override
    public void create(GatherTaskDto gatherTaskDto) {
        if (gatherPlanRepository.countByNameEquals(gatherTaskDto.getName()) > 0) {
            throw new BadRequestException("计划名称重复");
        }
        GatherPlan gatherPlan = mapper.toEntity(gatherTaskDto);
        gatherPlan.setTaskRunningNum(0);
        //状态
        gatherPlan.setStatus(0);
        //采集对象
        List<Asset> assetList = new ArrayList<>();
        assetList = getAssets(gatherPlan, assetList);
        if (assetList.isEmpty()) {
            throw new BadRequestException("创建失败，只支持ssh协议采集");
        }
        gatherPlan.setAssetList(assetList);
        gatherPlan.setGatherNum(assetList.size());
        gatherPlan = gatherPlanRepository.save(gatherPlan);
        //新建采集周期
        GatherPeriod period = new GatherPeriod();
        //非手动执行
        if (gatherPlan.getExecution() != 1) {
            //创建job
            String cron = getCron(gatherTaskDto);
            //采集计划
            period.setGatherPlan(gatherPlan);
            //开始时间
            period.setStartTime(gatherTaskDto.getStartTime());
            switch (gatherPlan.getExecution()) {
                //手动 不做处理
                case 1:
                    break;
                //自定义
                case 4:
                    period.setUnit("day");
                    period.setPeriod(Integer.parseInt(gatherTaskDto.getExecuteParam()));
                    break;
                //每周
                case 2:
                    period.setCron(cron);
                    break;
                //每月
                case 3:
                    period.setCron(cron);
                    break;
                default:
            }
        }
        //实时
        period.setContent("frequently");
        gatherPeriodRepository.save(period);
        //耗时
        GatherPeriod periodStationary = GatherPeriod.copy(period);
        periodStationary.setContent("stationary");
        gatherPeriodRepository.save(periodStationary);
        //不常变化
        GatherPeriod periodSeldom = GatherPeriod.copy(period);
        periodSeldom.setContent("seldom");
        gatherPeriodRepository.save(periodSeldom);
    }

    /**
     * 创建job
     *
     * @param gatherTaskDto 采集周期
     * @return job
     */
    public String getCron(GatherTaskDto gatherTaskDto) {
        String cron = "";
        //cron表达式或者间隔处理
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(gatherTaskDto.getStartTime().getTime());
        //执行参数处理
        String[] strs = gatherTaskDto.getExecuteParam().split(",");
        List<Integer> paramList = new ArrayList<>();
        for (String str : strs) {
            if (!str.isEmpty()) {
                paramList.add(Integer.parseInt(str));
            }
        }
        //构建cron表达式模型
        TaskScheduleModel model = new TaskScheduleModel();
        model.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        model.setMinute(calendar.get(Calendar.MINUTE));
        model.setSecond(calendar.get(Calendar.SECOND));
        switch (gatherTaskDto.getExecution()) {
            //手动 不做处理
            case 1:
                break;
            //自定义
            case 4:
                break;
            //每周
            case 2:
                model.setJobType(TaskScheduleModel.WEEKLY);
                model.setDayOfWeeks(paramList);
                cron = CronUtil.createCronExpression(model);
                break;
            //每月
            case 3:
                model.setJobType(TaskScheduleModel.MONTHLY);
                model.setDayOfMonths(paramList);
                cron = CronUtil.createCronExpression(model);
                break;
            default:
        }
        return cron;
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    @Transactional(rollbackOn = Exception.class)
    @Override
    public void removeById(Long id) {
        GatherPlan plan = gatherPlanRepository.findById(id).orElse(null);
        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("Gather plan id is null: " + id);
            }
            return;
        }
        Integer status = plan.getStatus();
        if (status.equals(GatherConstants.STATE_UNSTART) || status.equals(GatherConstants.STATE_END)) {
            gatherPlanRepository.deleteById(id);
            //删除计划同时删除采集结果
            gatherTaskManager.remove(id);
            Query query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("planId", plan.getId()))).build();
            esTemplate.delete(query, GatherResultEs.class);
        } else {
            throw new BadRequestException("删除失败，正在执行");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @param delAll
     * @return
     */
    @Transactional(rollbackOn = Exception.class)
    @Override
    public void removeBatch(Set<Long> ids, Boolean delAll) {
        if (delAll) {
            gatherPlanRepository.findAll().forEach(e -> removeById(e.getId()));
        } else {
            ids.forEach(this::removeById);
        }
    }

    /**
     * 根据id修改
     *
     * @param gatherTaskDto 发现任务传输对象
     */
    @Override
    public void updateById(GatherTaskDto gatherTaskDto) {
        GatherPlan newPlan = mapper.toEntity(gatherTaskDto);
        GatherPlan oldPlan = gatherPlanRepository.findById(gatherTaskDto.getId()).orElse(null);
        if (null == oldPlan) {
            throw new BadRequestException("当前计划不存在");
        }
        //采集对象
        List<Asset> assetList = new ArrayList<>();
        switch (newPlan.getGatherObj()) {
            case 1:
                //全部
                assetList = assetRepository.findAll();
                newPlan.setAssetList(assetList);
                break;
            case 2:
                //按部门查询
                String[] deptIds = newPlan.getDeptList().split(",");
                Set<Long> idSet = new HashSet<>();
                for (String deptId : deptIds) {
                    idSet.add(Long.parseLong(deptId));
                }
                List<Dept> depts = deptRepository.findAllById(idSet);
                assetRepository.findAllByDeptIn(depts);
                newPlan.setAssetList(assetList);
                break;
            default:
        }
        newPlan.setGatherNum(assetList.size());
        Integer newExecution = newPlan.getExecution();
        //非手动执行
        if (newExecution != 1) {
            //创建job
            String cron = getCron(gatherTaskDto);
            switch (newExecution) {
                //自定义
                case 4:
                    oldPlan.getGatherPeriods().forEach(gatherPeriod -> {
                        gatherPeriod.setStartTime(gatherTaskDto.getStartTime());
                        gatherPeriod.setUnit("day");
                        gatherPeriod.setPeriod(Integer.parseInt(gatherTaskDto.getExecuteParam()));
                        gatherPeriod.setCron(null);
                    });
                    break;
                //2每周 3每月
                case 2:
                case 3:
                    oldPlan.getGatherPeriods().forEach(gatherPeriod -> {
                        gatherPeriod.setStartTime(gatherTaskDto.getStartTime());
                        gatherPeriod.setCron(cron);
                        gatherPeriod.setUnit(null);
                        gatherPeriod.setPeriod(null);
                    });
                    break;
                default:
            }
        } else {
            oldPlan.getGatherPeriods().forEach(gatherPeriod -> {
                gatherPeriod.setCron(null);
                gatherPeriod.setUnit(null);
                gatherPeriod.setPeriod(null);
            });
        }
        newPlan.setGatherPeriods(oldPlan.getGatherPeriods());
        newPlan.setId(oldPlan.getId());
        gatherPlanRepository.save(newPlan);
    }

    /**
     * 根据id开始任务
     *
     * @param id
     */
    @Override
    public void start(Long id) {
        gatherTaskManager.runNow(gatherPlanRepository.findById(id).orElse(null), 1L);
        this.updatePlanState(id, GatherConstants.STATE_RUNNING);
    }

    /**
     * 根据资产ID开始任务
     *
     * @param id 资产ID
     */
    @Override
    public void startByAssetId(Long id) {
        GatherPlan gatherPlan = new GatherPlan();
        Asset asset = assetRepository.findById(id).orElse(null);
        if (null == asset) {
            throw new BadRequestException("没有此资产");
        }
        gatherPlan.setAssetList(new ArrayList<>(Collections.singletonList(asset)));
        gatherPlan.setName(asset.getName());
        gatherPlan.setExecution(1);
        gatherPlan.setStatus(0);
        gatherPlan.setTaskRunningNum(0);
        gatherPlan.setCreateTime(new Timestamp(System.currentTimeMillis()));
        gatherPlan.setCreateBy(SecurityUtils.getCurrentUsername());
        gatherPlan.setUpdateBy(SecurityUtils.getCurrentUsername());
        gatherPlan.setGatherObj(3);
        gatherPlan.setGatherNum(1);
        gatherPlan.setStartTime(new Timestamp(System.currentTimeMillis()));
        gatherPlanRepository.save(gatherPlan);
        gatherTaskManager.runNow(gatherPlan, SecurityUtils.getCurrentUserId());
//         this.updatePlanState(id, GatherConstants.STATE_RUNNING);
    }

    /**
     * 批量开始
     *
     * @param ids
     * @param startAll
     */
    @Override
    public void startBatch(Set<Long> ids, Boolean startAll) {
        if (startAll) {
            gatherPlanRepository.findAll().forEach(e -> start(e.getId()));
        } else {
            ids.forEach(this::start);
        }
    }

    /**
     * 暂停/继续
     *
     * @param id
     */
    @Transactional(rollbackOn = Exception.class)
    @Override
    public void pause(Long id) {
        GatherPlan plan = gatherPlanRepository.findById(id).orElse(null);
        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("Gather plan id is null: " + id);
            }
            return;
        }
        Integer status = plan.getStatus();
        //暂停 -> 开始
        if (status.equals(GatherConstants.STATE_STOP)) {
            this.updatePlanState(id, GatherConstants.STATE_RUNNING);
        } else if (status.equals(GatherConstants.STATE_RUNNING)) {
            //开始 -> 暂停
            this.updatePlanState(id, GatherConstants.STATE_STOP);
        }
    }

    /**
     * 取消
     *
     * @param id
     */
    @Transactional(rollbackOn = Exception.class)
    @Override
    public void cancel(Long id) {
        GatherPlan plan = gatherPlanRepository.findById(id).orElse(null);
        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("Gather plan id is null: " + id);
            }
            return;
        }
        if (gatherTaskManager.cancel(id)) {
            this.updatePlanState(id, GatherConstants.STATE_END);
        }
    }

    @Override
    public void judgeAndUpdatePlanStatus(Long planId, boolean taskStatus) {
        GatherPlan plan = gatherPlanRepository.findById(planId).orElse(null);
        if (null == plan) {
            if (log.isWarnEnabled()) {
                log.warn("Gather plan id is null: " + planId);
            }
            return;
        }
        int num = 0;
        if (null != plan.getTaskRunningNum()) {
            num = plan.getTaskRunningNum();
        }
        if (taskStatus) {
            num++;
        } else {
            if (num > 0) {
                num--;
            }
        }
        if (!GatherConstants.STATE_STOP.equals(plan.getStatus())) {
            // 暂停状态的计划不变更状态
            if (num == 0) {
                plan.setStatus(GatherConstants.STATE_END);
            } else {
                plan.setStatus(GatherConstants.STATE_RUNNING);
            }
        }
        plan.setTaskRunningNum(num);
        gatherPlanRepository.save(plan);
    }

    @Override
    public void updatePlanState(Long planId, Integer status) {
        if (null == planId) {
            return;
        }
        gatherPlanRepository.updateState(Collections.singletonList(planId), status);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public Optional<GatherPlan> findByIdHasAssetList(Long id) {
        final Optional<GatherPlan> gatherPlanOpt = gatherPlanRepository.findById(id);
        gatherPlanOpt.ifPresent(gatherPlan -> log.info("size{}", gatherPlan.getAssetList().size()));
        return gatherPlanOpt;
    }

    /**
     * 根据名称查重
     *
     * @param name 名称
     * @return true 不重复 false 重复
     */
    @Override
    public boolean checkName(String name) {
        return gatherPlanRepository.countByNameEquals(name) == 0 ? true : false;
    }

    /**
     * 根据任务id查询资产列表
     *
     * @param id 任务id
     * @return 资产列表
     */
    @Override
    public Object findAssetsById(Long id, Pageable pageable) {
        return assetRepository.findAssetsById(id, pageable).map(assetMapper::toDto);
    }

    @Override
    public String isCreate(GatherTaskDto gatherTaskDto) {
        GatherPlan gatherPlan = mapper.toEntity(gatherTaskDto);
        //采集对象
        List<Asset> assetList = new ArrayList<>();
        String ipNumList = "";
        assetList = getAssets(gatherPlan, assetList);
        if (assetList.size() > ScanPlan.PLAN_ASSET_MAX_SUM) {
            ipNumList = BadRequestException.HINT;
        }
        return ipNumList;
    }

    /**
     * 获取资产数量
     *
     * @param gatherPlan 采集对象
     * @param assetList  资产列表
     * @return 结果
     */
    private List<Asset> getAssets(GatherPlan gatherPlan, List<Asset> assetList) {
        switch (gatherPlan.getGatherObj()) {
            case 1:
                //全部
                assetList = assetRepository.findAll();
                break;
            case 2:
                //按部门查询
                String[] deptIds = gatherPlan.getDeptList().split(",");
                Set<Long> idSet = new HashSet<>();
                for (String deptId : deptIds) {
                    idSet.add(Long.parseLong(deptId));
                }
                List<Dept> depts = deptRepository.findAllById(idSet);
                assetList = assetRepository.findAllByDeptIn(depts);
                break;
            case 3:
                //自定义 根据id查询资产
                Set<Long> ids = new HashSet<>();
                gatherPlan.getAssetList().forEach(asset -> ids.add(asset.getId()));
                assetList = assetRepository.findAllById(ids);
            default:
        }
        if (assetList.isEmpty()) {
            throw new BadRequestException("创建失败，当前配置无资产");
        }
        //资产校验 ssh
        assetList = assetList.stream().filter(asset -> {
            AssetConfig config = JSONObject.parseObject(asset.getConfig(), AssetConfig.class);
            if (config.getGatherType().contains(DetectConstant.GATHER_TYPE_LOGIN) &&
                    config.getLogin().getProtocol().equals(DetectConstant.PROTOCOL_SSH)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return assetList;
    }
}
