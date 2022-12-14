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
 * ???????????????????????????
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class GatherPlanServiceImpl implements GatherPlanService {

    /**
     * ??????????????????????????????????????????????????????
     */
    @Resource
    private GatherTaskMapper mapper;

    /**
     * ????????????????????????????????????????????????
     */
    @Resource
    private AssetMapper assetMapper;

    /**
     * ??????????????????????????????
     */
    @Resource
    private GatherPlanRepository gatherPlanRepository;

    /**
     * ??????????????????????????????
     */
    @Resource
    private GatherPeriodRepository gatherPeriodRepository;

    /**
     * ?????????????????????
     */
    @Lazy
    @Resource
    private GatherTaskManager gatherTaskManager;

    /**
     * ????????????????????????
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * ????????????????????????
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * ES??????
     */
    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    /**
     * ??????????????????
     *
     * @param criteria ????????????
     * @param pageable ????????????
     * @return ??????????????????
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
     * ??????????????????
     *
     * @param gatherTaskDto ????????????????????????
     */
    @Override
    public void create(GatherTaskDto gatherTaskDto) {
        if (gatherPlanRepository.countByNameEquals(gatherTaskDto.getName()) > 0) {
            throw new BadRequestException("??????????????????");
        }
        GatherPlan gatherPlan = mapper.toEntity(gatherTaskDto);
        gatherPlan.setTaskRunningNum(0);
        //??????
        gatherPlan.setStatus(0);
        //????????????
        List<Asset> assetList = new ArrayList<>();
        assetList = getAssets(gatherPlan, assetList);
        if (assetList.isEmpty()) {
            throw new BadRequestException("????????????????????????ssh????????????");
        }
        gatherPlan.setAssetList(assetList);
        gatherPlan.setGatherNum(assetList.size());
        gatherPlan = gatherPlanRepository.save(gatherPlan);
        //??????????????????
        GatherPeriod period = new GatherPeriod();
        //???????????????
        if (gatherPlan.getExecution() != 1) {
            //??????job
            String cron = getCron(gatherTaskDto);
            //????????????
            period.setGatherPlan(gatherPlan);
            //????????????
            period.setStartTime(gatherTaskDto.getStartTime());
            switch (gatherPlan.getExecution()) {
                //?????? ????????????
                case 1:
                    break;
                //?????????
                case 4:
                    period.setUnit("day");
                    period.setPeriod(Integer.parseInt(gatherTaskDto.getExecuteParam()));
                    break;
                //??????
                case 2:
                    period.setCron(cron);
                    break;
                //??????
                case 3:
                    period.setCron(cron);
                    break;
                default:
            }
        }
        //??????
        period.setContent("frequently");
        gatherPeriodRepository.save(period);
        //??????
        GatherPeriod periodStationary = GatherPeriod.copy(period);
        periodStationary.setContent("stationary");
        gatherPeriodRepository.save(periodStationary);
        //????????????
        GatherPeriod periodSeldom = GatherPeriod.copy(period);
        periodSeldom.setContent("seldom");
        gatherPeriodRepository.save(periodSeldom);
    }

    /**
     * ??????job
     *
     * @param gatherTaskDto ????????????
     * @return job
     */
    public String getCron(GatherTaskDto gatherTaskDto) {
        String cron = "";
        //cron???????????????????????????
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(gatherTaskDto.getStartTime().getTime());
        //??????????????????
        String[] strs = gatherTaskDto.getExecuteParam().split(",");
        List<Integer> paramList = new ArrayList<>();
        for (String str : strs) {
            if (!str.isEmpty()) {
                paramList.add(Integer.parseInt(str));
            }
        }
        //??????cron???????????????
        TaskScheduleModel model = new TaskScheduleModel();
        model.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        model.setMinute(calendar.get(Calendar.MINUTE));
        model.setSecond(calendar.get(Calendar.SECOND));
        switch (gatherTaskDto.getExecution()) {
            //?????? ????????????
            case 1:
                break;
            //?????????
            case 4:
                break;
            //??????
            case 2:
                model.setJobType(TaskScheduleModel.WEEKLY);
                model.setDayOfWeeks(paramList);
                cron = CronUtil.createCronExpression(model);
                break;
            //??????
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
     * ??????id??????
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
            //????????????????????????????????????
            gatherTaskManager.remove(id);
            Query query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("planId", plan.getId()))).build();
            esTemplate.delete(query, GatherResultEs.class);
        } else {
            throw new BadRequestException("???????????????????????????");
        }
    }

    /**
     * ????????????
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
     * ??????id??????
     *
     * @param gatherTaskDto ????????????????????????
     */
    @Override
    public void updateById(GatherTaskDto gatherTaskDto) {
        GatherPlan newPlan = mapper.toEntity(gatherTaskDto);
        GatherPlan oldPlan = gatherPlanRepository.findById(gatherTaskDto.getId()).orElse(null);
        if (null == oldPlan) {
            throw new BadRequestException("?????????????????????");
        }
        //????????????
        List<Asset> assetList = new ArrayList<>();
        switch (newPlan.getGatherObj()) {
            case 1:
                //??????
                assetList = assetRepository.findAll();
                newPlan.setAssetList(assetList);
                break;
            case 2:
                //???????????????
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
        //???????????????
        if (newExecution != 1) {
            //??????job
            String cron = getCron(gatherTaskDto);
            switch (newExecution) {
                //?????????
                case 4:
                    oldPlan.getGatherPeriods().forEach(gatherPeriod -> {
                        gatherPeriod.setStartTime(gatherTaskDto.getStartTime());
                        gatherPeriod.setUnit("day");
                        gatherPeriod.setPeriod(Integer.parseInt(gatherTaskDto.getExecuteParam()));
                        gatherPeriod.setCron(null);
                    });
                    break;
                //2?????? 3??????
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
     * ??????id????????????
     *
     * @param id
     */
    @Override
    public void start(Long id) {
        gatherTaskManager.runNow(gatherPlanRepository.findById(id).orElse(null), 1L);
        this.updatePlanState(id, GatherConstants.STATE_RUNNING);
    }

    /**
     * ????????????ID????????????
     *
     * @param id ??????ID
     */
    @Override
    public void startByAssetId(Long id) {
        GatherPlan gatherPlan = new GatherPlan();
        Asset asset = assetRepository.findById(id).orElse(null);
        if (null == asset) {
            throw new BadRequestException("???????????????");
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
     * ????????????
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
     * ??????/??????
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
        //?????? -> ??????
        if (status.equals(GatherConstants.STATE_STOP)) {
            this.updatePlanState(id, GatherConstants.STATE_RUNNING);
        } else if (status.equals(GatherConstants.STATE_RUNNING)) {
            //?????? -> ??????
            this.updatePlanState(id, GatherConstants.STATE_STOP);
        }
    }

    /**
     * ??????
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
            // ????????????????????????????????????
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
     * ??????????????????
     *
     * @param name ??????
     * @return true ????????? false ??????
     */
    @Override
    public boolean checkName(String name) {
        return gatherPlanRepository.countByNameEquals(name) == 0 ? true : false;
    }

    /**
     * ????????????id??????????????????
     *
     * @param id ??????id
     * @return ????????????
     */
    @Override
    public Object findAssetsById(Long id, Pageable pageable) {
        return assetRepository.findAssetsById(id, pageable).map(assetMapper::toDto);
    }

    @Override
    public String isCreate(GatherTaskDto gatherTaskDto) {
        GatherPlan gatherPlan = mapper.toEntity(gatherTaskDto);
        //????????????
        List<Asset> assetList = new ArrayList<>();
        String ipNumList = "";
        assetList = getAssets(gatherPlan, assetList);
        if (assetList.size() > ScanPlan.PLAN_ASSET_MAX_SUM) {
            ipNumList = BadRequestException.HINT;
        }
        return ipNumList;
    }

    /**
     * ??????????????????
     *
     * @param gatherPlan ????????????
     * @param assetList  ????????????
     * @return ??????
     */
    private List<Asset> getAssets(GatherPlan gatherPlan, List<Asset> assetList) {
        switch (gatherPlan.getGatherObj()) {
            case 1:
                //??????
                assetList = assetRepository.findAll();
                break;
            case 2:
                //???????????????
                String[] deptIds = gatherPlan.getDeptList().split(",");
                Set<Long> idSet = new HashSet<>();
                for (String deptId : deptIds) {
                    idSet.add(Long.parseLong(deptId));
                }
                List<Dept> depts = deptRepository.findAllById(idSet);
                assetList = assetRepository.findAllByDeptIn(depts);
                break;
            case 3:
                //????????? ??????id????????????
                Set<Long> ids = new HashSet<>();
                gatherPlan.getAssetList().forEach(asset -> ids.add(asset.getId()));
                assetList = assetRepository.findAllById(ids);
            default:
        }
        if (assetList.isEmpty()) {
            throw new BadRequestException("????????????????????????????????????");
        }
        //???????????? ssh
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
