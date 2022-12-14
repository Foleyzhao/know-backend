package com.cumulus.modules.business.detect.service.impl;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.detect.common.DetectManager;
import com.cumulus.modules.business.detect.common.DetectSendBean;
import com.cumulus.modules.business.detect.dto.DetectRequest;
import com.cumulus.modules.business.detect.dto.DetectTaskDto;
import com.cumulus.modules.business.detect.dto.DetectTaskIpDto;
import com.cumulus.modules.business.detect.dto.DetectTaskQueryCriteria;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.business.detect.mapper.DetectTaskMapper;
import com.cumulus.modules.business.detect.repository.DetectTaskRepository;
import com.cumulus.modules.business.detect.service.DetectTaskService;
import com.cumulus.modules.business.entity.IpLibrary;
import com.cumulus.modules.business.repository.AssetConfirmRepository;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.modules.business.utils.CronUtil;
import com.cumulus.modules.business.utils.TaskScheduleModel;
import com.cumulus.modules.business.vulnerability.entity.ScanPlan;
import com.cumulus.modules.quartz.entity.QuartzJob;
import com.cumulus.modules.quartz.entity.QuartzJobEnum;
import com.cumulus.modules.quartz.service.QuartzJobService;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RegexUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ????????????????????????
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class DetectTaskServiceImpl implements DetectTaskService {

    /**
     * ????????????
     */
    public static final LinkedBlockingQueue<List<DetectRequest>> taskResult = new LinkedBlockingQueue<>(1000);
    /**
     * ??????????????????????????????
     */
    @Resource
    private DetectTaskRepository repository;

    /**
     * ??????????????????????????????????????????????????????
     */
    @Resource
    private DetectTaskMapper mapper;

    /**
     * ????????????????????????
     */
    @Autowired
    private QuartzJobService quartzJobService;

    /**
     * mq
     */
    @Autowired
    private DetectSendBean detectSendBean;

    /**
     * ??????????????????
     */
    @Autowired
    private DetectManager detectManager;

    /**
     * ip?????????????????????
     */
    @Autowired
    private IpLibraryRepository ipLibraryRepository;

    /**
     * ????????????????????????
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * ??????????????????????????????
     */
    @Autowired
    private AssetConfirmRepository assetConfirmRepository;

    /**
     * redis??????
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * ??????ip
     *
     * @return
     */
    public static DetectTaskIpDto getDetectTaskIpDto(String IpList) {
        DetectTaskIpDto detectTaskIpDto = new DetectTaskIpDto();
        List<String> maskList = new ArrayList<>();
        List<String> ipList = new ArrayList<>();
        List<DetectTaskIpDto.IpSegmentList> manySegmentList = new ArrayList<>();
        String[] ips = IpList.split(",");
        for (String ip : ips) {
            if (ip.contains(":")) {
                ipList.add(ip);
                continue;
            }
            String[] split = ip.split("\\.");
            if (ip.contains("/")) {
                maskList.add(ip);
                detectTaskIpDto.setIpMaskList(maskList);
            } else if (split[0].contains("-") || split[1].contains("-") || split[2].contains("-")) {
                DetectTaskIpDto.IpSegmentList ipSegment = new DetectTaskIpDto.IpSegmentList();

                DetectTaskIpDto.IpSegmentInterval ip1 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip2 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip3 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip4 = new DetectTaskIpDto.IpSegmentInterval();
                if (split[0].contains("-")) {
                    String[] oneSplit = split[0].split("-");
                    ip1.setMax(Integer.parseInt(oneSplit[1]));
                    ip1.setMin(Integer.parseInt(oneSplit[0]));
                } else {
                    ip1.setMax(Integer.parseInt(split[0]));
                    ip1.setMin(Integer.parseInt(split[0]));
                }
                if (split[1].contains("-")) {
                    String[] twoSplit = split[1].split("-");
                    ip2.setMax(Integer.parseInt(twoSplit[1]));
                    ip2.setMin(Integer.parseInt(twoSplit[0]));
                } else {
                    ip2.setMax(Integer.parseInt(split[1]));
                    ip2.setMin(Integer.parseInt(split[1]));
                }
                if (split[2].contains("-")) {
                    String[] threeSplit = split[2].split("-");
                    ip3.setMax(Integer.parseInt(threeSplit[1]));
                    ip3.setMin(Integer.parseInt(threeSplit[0]));
                } else {
                    ip3.setMax(Integer.parseInt(split[2]));
                    ip3.setMin(Integer.parseInt(split[2]));
                }
                if (split[3].contains("-")) {
                    String[] fourSplit = split[3].split("-");
                    ip4.setMax(Integer.parseInt(fourSplit[1]));
                    ip4.setMin(Integer.parseInt(fourSplit[0]));
                } else {
                    ip4.setMax(Integer.parseInt(split[3]));
                    ip4.setMin(Integer.parseInt(split[3]));
                }
                ipSegment.setOne(ip1.getMin());
                ipSegment.setTwo(ip2.getMin());
                ipSegment.setThree(ip3.getMin());
                ipSegment.setFour(ip4.getMin());
                ipSegment.setIp1(ip1);
                ipSegment.setIp2(ip2);
                ipSegment.setIp3(ip3);
                ipSegment.setIp4(ip4);
                ipSegment.setIpSegment(ip);
                manySegmentList.add(ipSegment);
                detectTaskIpDto.setIpSegmentList(manySegmentList);
            } else {
                ipList.add(ip);
                detectTaskIpDto.setIpList(ipList);
            }
        }
        return detectTaskIpDto;
    }


    public static void getIplist(DetectTask detectTask, String ipList2) {
        if (detectTask.getIpRange() == DetectConstant.IP_DIY) {
            String[] split = ipList2.split(",");
            Set<String> set = new HashSet<>(Arrays.asList(split));
            isIp(set);
            String ipList = set.toString().substring(0, set.toString().length() - 1).substring(1);
            detectTask.setIpList(ipList);
        }
    }

    /**
     * ??????ip?????????????????????
     *
     * @param set
     */
    public static void isIp(Set<String> set) {
        for (String ip : set) {
            if (ip.matches(RegexUtil.IPV4_PARAGRAPH_PATTERN)) {
                String[] split1 = ip.split("\\.");
                if (split1[3].contains(RegexUtil.IPV4_PARAGRAPH_STR)) {
                    List<Object> ipv4ParagraphInfo = RegexUtil.getIpv4ParagraphInfo(ip);
                    if (null == ipv4ParagraphInfo) {
                        throw new BadRequestException("IP????????????. IP = " + ip);
                    }
                }
            }

        }
    }

    /**
     * ??????????????????
     *
     * @param criteria ????????????
     * @param pageable ????????????
     * @return ??????????????????
     */
    @Override
    public Object queryAll(DetectTaskQueryCriteria criteria, Pageable pageable) {
        Page<DetectTask> detectTaskPage = repository.findAll((root, query, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), pageable);
        return detectTaskPage.map(e -> {
            DetectTaskDto dto = mapper.toDto(e);
            if (dto.getTaskStatus() == DetectConstant.TASK_STATUS_RUN) {
                String num = redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + dto.getId()).
                        get(DetectConstant.REDIS_KEY_NUM).toString();
                String done = redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + dto.getId()).
                        get(DetectConstant.REDIS_KEY_DONE).toString();
                dto.setProgress(done + DetectConstant.SEPARATOR + num);
                NumberFormat numberFormat = NumberFormat.getNumberInstance();
                numberFormat.setMaximumFractionDigits(0);
                dto.setPercentage(Integer.parseInt(
                        numberFormat.format(Double.parseDouble(done) / Double.parseDouble(num) * 100)));

            }
            return dto;
        });
    }

    /**
     * ??????????????????
     *
     * @param detectTaskDto ????????????????????????
     */
    @Override
    public void create(DetectTaskDto detectTaskDto) {
        //??????????????????
        if (!checkName(detectTaskDto.getDetectTaskName())) {
            throw new BadRequestException("???????????????????????????");
        }
        //??????job
        QuartzJob job = createJob(detectTaskDto);
        DetectTask detectTask = mapper.toEntity(detectTaskDto);
        detectTask.setCancel(false);
        detectTask.setCron(job.getCronExpression());
        detectTask.setStartTime(job.getStartTime());
        //????????? ip??????
        if (detectTask.getIpRange() == DetectConstant.IP_DEPT && !checkIpOfDept(detectTask)) {
            throw new BadRequestException("???????????????ip??????");
        }
        getNextTime(detectTask);
        detectTask.setNum(0);
        getIplist(detectTask, detectTaskDto.getIpList());
        detectTask = repository.save(detectTask);
        long ipNum = 0;
        switch (detectTask.getIpRange()) {
            case DetectConstant.IP_ALL:
                ipNum = ipLibraryRepository.count();
                break;
            case DetectConstant.IP_DEPT:
                String deptList = detectTask.getDeptList();
                Set<String> set = new HashSet<>(Collections.singletonList(deptList));
                ipNum = ipLibraryRepository.countNum(set);
                break;
            case DetectConstant.IP_DIY:
                String ipList = detectTask.getIpList();
                ipNum = RegexUtil.getNum(ipList);
                break;
            default:
        }
        //???????????????????????????
        if (detectTask.getTaskType() != DetectConstant.TASK_TYPE_MANUAL) {
            job.setParams(detectTask.getId());
            job.setJobName(detectTask.getDetectTaskName());
            quartzJobService.create(job);
        }
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTask.getId()).
                put(DetectConstant.REDIS_KEY_NUM, ipNum);
    }

    /**
     * ????????????????????????
     * ???????????????????????? ????????????????????????
     *
     * @param detectTask ????????????
     */
    private void getNextTime(DetectTask detectTask) {
        //?????? ?????? cron
        if (detectTask.getTaskType() == DetectConstant.TASK_TYPE_WEEKLY ||
                detectTask.getTaskType() == DetectConstant.TASK_TYPE_MONTHLY) {
            detectTask.setNextTime(CronUtil.getNextTime(detectTask.getCron()));
        }
        //????????? ???????????????
        if (detectTask.getTaskType() == DetectConstant.TASK_TYPE_DIY) {
            //???????????? ????????????
            Calendar next = Calendar.getInstance();
            next.setTimeInMillis(detectTask.getStartTime().getTime());
            //???????????? ??????
            Calendar current = Calendar.getInstance();
            next.set(Calendar.DAY_OF_YEAR,
                    current.get(Calendar.DAY_OF_YEAR) + Integer.parseInt(detectTask.getExecuteParam()));
            //????????????????????????
            detectTask.setNextTime(Timestamp.from(next.toInstant()));
        }
    }

    /**
     * ??????????????? ????????????????????????ip??????
     *
     * @param detectTask ????????????
     * @return true ???ip  false ???ip
     */
    private boolean checkIpOfDept(DetectTask detectTask) {
        String[] deptIds = detectTask.getDeptList().split(",");
        Set<Long> set = new HashSet<>();
        for (String deptId : deptIds) {
            set.add(Long.parseLong(deptId));
        }
        if (ipLibraryRepository.queryIpByDept(set).isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * ??????????????????
     *
     * @param param ????????????id
     */
    @Transactional(rollbackFor = Exception.class)
    public void doDetectTask(Long param) {
        log.info("?????????????????? id:{}", param);
        DetectTask detectTask = repository.findById(param).orElse(null);
        if (null == detectTask) {
            log.info("????????????????????????id:" + param);
        }
        detectTask.setNum(detectTask.getNum() + 1);
        switch (detectTask.getTaskStatus()) {
            case DetectConstant.TASK_STATUS_RUN:
                log.info("????????????????????????id:" + param);
                return;
            case DetectConstant.TASK_STATUS_PAUSE:
                log.info("???????????????????????????id:" + param);
                detectTask.setNum(detectTask.getNum() - 1);
                break;
            default:
        }
        //???????????????????????????
        getParse(detectTask);
        //??????????????????
        detectManager.startRecord(detectTask);
        //???????????? ??????????????????????????????
        if (detectTask.getTaskStatus() != DetectConstant.TASK_STATUS_PAUSE) {
            getNextTime(detectTask);
        }
        //?????????????????? ????????????
        detectTask.setTaskStatus(DetectConstant.TASK_STATUS_RUN);
        repository.save(detectTask);
    }

    /**
     * ?????? ip
     *
     * @param detectTask
     */
    private void getParse(DetectTask detectTask) {
        DetectTaskIpDto detectTaskIpDto = null;
        switch (detectTask.getIpRange()) {
            case DetectConstant.IP_ALL:
            case DetectConstant.IP_DEPT:
                detectTaskIpDto = new DetectTaskIpDto();
                break;
            case DetectConstant.IP_DIY:
                detectTaskIpDto = getDetectTaskIpDto(detectTask.getIpList());
                break;
            default:
        }
        detectTaskIpDto.setIpLib(true);
        DetectTaskIpDto finalDetectTaskIpDto = detectTaskIpDto;
        Thread gatherThread = new Thread(() -> {
            while (true) {
                try {
                    // ??????????????????????????????
                    if (finalDetectTaskIpDto.isFLAG()) {
                        try {
                            log.info("Parsing is complete");
                            throw new BadRequestException("Parsing is complete");
                        } catch (Exception e) {
                            break;
                        }
                    }
                    detectManager.toParam(detectTask, finalDetectTaskIpDto);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Parse GatherThread error result", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("Parse GatherThread sleep error result", ex);
                        }
                    }
                }
            }
        });
        gatherThread.setDaemon(true);
        gatherThread.setName("PARSE_IP_THREAD_TASK_RESULT_LOGIN");
        gatherThread.start();


    }

    @PostConstruct
    public void init() {
        Thread gatherThread = new Thread(() -> {
            while (true) {
                try {
                    List<DetectRequest> take = taskResult.take();
                    //???????????????mq
                    take.forEach(detectRequest -> detectSendBean.sendRequestForAsyncResponse(detectRequest));
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("send mq GatherThread error result", e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("send mq GatherThread sleep error result", ex);
                        }
                    }
                }
            }
        });
        gatherThread.setDaemon(true);
        gatherThread.setName("SEND_IP_THREAD_TASK_RESULT_LOGIN");
        gatherThread.start();
    }

    /**
     * ??????id??????
     *
     * @param id ??????id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        //??????????????????
        DetectTask detectTask = repository.findById(id).orElse(null);
        if (null == detectTask) {
            throw new BadRequestException("?????????????????????");
        } else {
            switch (detectTask.getTaskStatus()) {
                case DetectConstant.TASK_STATUS_RUN:
                case DetectConstant.TASK_STATUS_PAUSE:
                    throw new BadRequestException("?????????????????????");
                case DetectConstant.TASK_STATUS_NONE:
                case DetectConstant.TASK_STATUS_END:
                    //??????????????????
                    repository.deleteById(id);
                    //????????????????????????????????????
                    assetConfirmRepository.updateTaskByTaskId(id);
                    QuartzJob job = quartzJobService.findByParamId(id);
                    Set<Long> ids = new HashSet<>();
                    if (null != job) {
                        ids.add(job.getId());
                        quartzJobService.delete(ids);
                    }
                    detectManager.delRedisDetectKey(id);
                    log.info("?????????????????? id:" + id);
                    break;
                default:
            }
        }
    }

    /**
     * ????????????
     *
     * @param ids    ??????id??????
     * @param delAll ??????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatch(Set<Long> ids, Boolean delAll) {
        if (delAll) {
            repository.findAll().forEach(e -> this.removeById(e.getId()));
        } else {
            ids.forEach(this::removeById);
        }
    }

    /**
     * ??????id??????
     *
     * @param detectTaskDto ????????????????????????
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateById(DetectTaskDto detectTaskDto) {
        //?????????
        DetectTask detectTask = repository.findById(detectTaskDto.getId()).orElse(null);
        //?????????
        DetectTask newTask = mapper.toEntity(detectTaskDto);
        newTask.setCancel(false);
        getIplist(newTask, newTask.getIpList());
        newTask.setNum(detectTask.getNum());
        if (newTask.getIpRange() == DetectConstant.IP_DEPT && !checkIpOfDept(newTask)) {
            throw new BadRequestException("???????????????ip??????");
        }
        //????????????ip??????
        long ipNum = 0;
        switch (newTask.getIpRange()) {
            case DetectConstant.IP_ALL:
                ipNum = ipLibraryRepository.count();
                break;
            case DetectConstant.IP_DEPT:
                String deptList = newTask.getDeptList();
                Set<String> set = new HashSet<>(Collections.singletonList(deptList));
                ipNum = ipLibraryRepository.countNum(set);
                break;
            case DetectConstant.IP_DIY:
                String ipList = newTask.getIpList();
                ipNum = RegexUtil.getNum(ipList);
                break;
            default:
        }
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTask.getId()).
                put(DetectConstant.REDIS_KEY_NUM, ipNum);
        QuartzJob job;
        //??????
        if (detectTask.getTaskType() == DetectConstant.TASK_TYPE_MANUAL) {
            //??????
            if (detectTaskDto.getTaskType() != DetectConstant.TASK_TYPE_MANUAL) {
                job = createJob(detectTaskDto);
                newTask.setCron(job.getCronExpression());
                newTask.setStartTime(job.getStartTime());
                //??????????????????
                getNextTime(newTask);
                newTask = repository.save(newTask);
                job.setParams(newTask.getId());
                job.setJobName(newTask.getDetectTaskName());
                quartzJobService.create(job);
            } else {
                //??????
                repository.save(newTask);
            }
        } else {
            //??????
            job = quartzJobService.findByParamId(detectTaskDto.getId());
            if (detectTaskDto.getTaskType() != DetectConstant.TASK_TYPE_MANUAL) {
                //??????
                QuartzJob newJob = createJob(detectTaskDto);
                newJob.setId(job.getId());
                newJob.setParams(newTask.getId());
                newJob.setJobName(newTask.getDetectTaskName());
                quartzJobService.update(newJob);
                //????????????cron????????? ????????????
                newTask.setCron(newJob.getCronExpression());
                newTask.setStartTime(newJob.getStartTime());
                //??????????????????
                getNextTime(newTask);
            } else {
                //??????
                Set<Long> set = new HashSet<>();
                set.add(job.getId());
                quartzJobService.delete(set);
            }
            repository.save(newTask);
        }
    }

    /**
     * ????????????????????????
     *
     * @param id ??????id;
     */
    @Override
    public void execute(Long id) {
        Optional<QuartzJob> job = Optional.ofNullable(quartzJobService.findByParamId(id));
        if (job.isPresent() && job.get().getPeriod() == null) {
            quartzJobService.execution(job.get());
        } else {
            doDetectTask(id);
        }

    }

    /**
     * ????????????????????????
     *
     * @param ids ??????id??????;
     */
    @Override
    public void execute(Set<Long> ids, Boolean all) {
        if (all) {
            repository.findAll().forEach(e -> this.execute(e.getId()));
        } else {
            ids.forEach(this::execute);
        }
    }

    /**
     * ??????/??????
     *
     * @param id ??????id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void pause(Long id) {
        DetectTask detectTask = repository.findById(id).orElse(null);
        if (null == detectTask) {
            log.info("?????????????????????");
            return;
        }
        //?????? / ??????
        Integer taskStatus = detectTask.getTaskStatus();
        switch (taskStatus) {
            case DetectConstant.TASK_STATUS_RUN:
                //????????????->??????
                repository.updateStatusById(DetectConstant.TASK_STATUS_PAUSE, id);
                //??????????????????
                detectSendBean.sendRequestForAsyncResponse(new DetectRequest.StopRequest(id.toString()));
                break;
            case DetectConstant.TASK_STATUS_PAUSE:
                //?????????????????? ??????->????????????
                doDetectTask(id);
                break;
            default:
        }
    }

    /**
     * ??????job
     *
     * @param detectTaskDto ????????????????????????
     * @return job
     */
    public QuartzJob createJob(DetectTaskDto detectTaskDto) {
        QuartzJob job = new QuartzJob();
        job.setJobType(QuartzJobEnum.ASSET_DETECT.getJobType());
        job.setBeanName(QuartzJobEnum.ASSET_DETECT.getBeanName());
        job.setMethodName(QuartzJobEnum.ASSET_DETECT.getMethodName());
        job.setIsPause(false);
        //cron???????????????????????????
        if (detectTaskDto.getTaskType() != DetectConstant.TASK_TYPE_MANUAL) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(detectTaskDto.getStartTime().getTime());
            //??????????????????
            String[] strs = detectTaskDto.getExecuteParam().split(",");
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
            //?????????????????? ?????????
            Calendar now = Calendar.getInstance();
            now.add(Calendar.DAY_OF_YEAR, 1);
            now.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
            now.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
            now.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
            job.setStartTime(Timestamp.from(now.toInstant()));
            switch (detectTaskDto.getTaskType()) {
                //?????????
                case DetectConstant.TASK_TYPE_DIY:
                    job.setPeriod(Long.parseLong(detectTaskDto.getExecuteParam()) * 3600 * 24);
                    break;
                //??????
                case DetectConstant.TASK_TYPE_WEEKLY:
                    model.setJobType(TaskScheduleModel.WEEKLY);
                    model.setDayOfWeeks(paramList);
                    job.setCronExpression(CronUtil.createCronExpression(model));
                    job.setDescription(CronUtil.createDescription(model));
                    break;
                //??????
                case DetectConstant.TASK_TYPE_MONTHLY:
                    model.setJobType(TaskScheduleModel.MONTHLY);
                    model.setDayOfMonths(paramList);
                    job.setCronExpression(CronUtil.createCronExpression(model));
                    job.setDescription(CronUtil.createDescription(model));
                    break;
                default:
            }
        }
        return job;
    }

    /**
     * ????????????
     *
     * @param id ??????id
     */
    @Override
    public void cancel(Long id) {
        //??????????????????
        detectSendBean.sendRequestForAsyncResponse(new DetectRequest.StopRequest(id.toString()));
        //??????????????????
        detectManager.cancelRecord(id);
    }

    /**
     * ??????????????????
     *
     * @param name ??????
     * @return true????????? false??????
     */
    @Override
    public boolean checkName(String name) {
        return repository.countByDetectTaskNameEquals(name) == 0;
    }

    @Override
    public String isCreate(DetectTaskDto detectTaskDto) {
        DetectTask detectTask = mapper.toEntity(detectTaskDto);
        getIplist(detectTask, detectTaskDto.getIpList());
        String ipNumList = "";
        switch (detectTask.getIpRange()) {
            case DetectConstant.IP_ALL:
                long countAll = ipLibraryRepository.count();
                if (countAll > ScanPlan.PLAN_ASSET_MAX_SUM) {
                    ipNumList = BadRequestException.HINT;
                }
                break;
            case DetectConstant.IP_DEPT:
                String deptList = detectTask.getDeptList();
                Set<String> set = new HashSet<>(Collections.singletonList(deptList));
                int countDept = ipLibraryRepository.countNum(set);
                if (countDept > ScanPlan.PLAN_ASSET_MAX_SUM) {
                    ipNumList = BadRequestException.HINT;
                }
                break;
            case DetectConstant.IP_DIY:
                String ipList = detectTask.getIpList();
                int countDiy = RegexUtil.getNum(ipList);
                if (countDiy > ScanPlan.PLAN_ASSET_MAX_SUM) {
                    ipNumList = BadRequestException.HINT;
                }
                break;
            default:
        }
        return ipNumList;
    }

}
