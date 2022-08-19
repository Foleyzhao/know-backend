package com.cumulus.modules.quartz.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.quartz.entity.QuartzJob;
import com.cumulus.modules.quartz.repository.QuartzJobRepository;
import com.cumulus.modules.quartz.service.QuartzJobService;
import com.cumulus.modules.quartz.utils.QuartzManage;
import com.cumulus.utils.ValidationUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 定时任务服务实现
 *
 * @author zhaoff
 */
@Service(value = "quartzJobService")
public class QuartzJobServiceImpl implements QuartzJobService {

    /**
     * 定时任务数据访问接口
     */
    @Autowired
    private QuartzJobRepository quartzJobRepository;

    /**
     * 定时任务管理中心
     */
    @Autowired
    private QuartzManage quartzManage;

    @Override
    public QuartzJob findById(Long id) {
        QuartzJob quartzJob = quartzJobRepository.findById(id).orElseGet(QuartzJob::new);
        ValidationUtils.isNull(quartzJob.getId(), "QuartzJob", "id", id);
        return quartzJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(QuartzJob resources) {
        boolean validCron = StringUtils.isNotBlank(resources.getCronExpression())
                && !CronExpression.isValidExpression(resources.getCronExpression());
        if (validCron && null == resources.getPeriod()) {
            throw new BadRequestException("cron表达式格式错误或执行间隔错误");
        }
        resources = quartzJobRepository.save(resources);
        quartzManage.addJob(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(QuartzJob resources) {
        boolean validCron = StringUtils.isNotBlank(resources.getCronExpression())
                && !CronExpression.isValidExpression(resources.getCronExpression());
        if (validCron && null == resources.getPeriod()) {
            throw new BadRequestException("cron表达式格式错误或执行间隔错误");
        }
        resources = quartzJobRepository.save(resources);
        quartzManage.updateJobCron(resources);
    }

    @Override
    public void updateIsPause(QuartzJob quartzJob) {
        if (quartzJob.getIsPause()) {
            quartzManage.resumeJob(quartzJob);
            quartzJob.setIsPause(false);
        } else {
            quartzManage.pauseJob(quartzJob);
            quartzJob.setIsPause(true);
        }
        quartzJobRepository.save(quartzJob);
    }

    @Override
    public void execution(QuartzJob quartzJob) {
        quartzManage.runJobNow(quartzJob);
    }

    /**
     * 根据参数ID查询定时任务
     *
     * @param id 参数ID
     * @return 定时任务
     */
    @Override
    public QuartzJob findByParamId(Long id) {
        QuartzJob quartzJob = quartzJobRepository.findQuartzJobByParamsEquals(id);
        return quartzJob;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            QuartzJob quartzJob = findById(id);
            quartzManage.deleteJob(quartzJob);
            quartzJobRepository.delete(quartzJob);
        }
    }

    @Override
    public Date getNextFireTime(Long id){
        QuartzJob job = quartzJobRepository.findById(id).orElse(null);
        if (null == job){
            return null;
        }
        long startTime = job.getStartTime().getTime();
        long now = System.currentTimeMillis();
        //开始时间大于当前时间是 下次执行时间是开始时间
        if (startTime > now){
            return new Date(startTime);
        }

        //cron 表达式
        if(StringUtils.isNotBlank(job.getCronExpression())){
            try{
                CronExpression cronExpression = new CronExpression(job.getCronExpression());
                return cronExpression.getNextValidTimeAfter(new Date());
            }catch (ParseException e){
                return null;
            }
        }else if (job.getPeriod() != null){
            //间隔时间
            long period = job.getPeriod() * 1000;
            long difference = now - startTime;
            long times = difference / period;
            long nextTime = startTime + period * times;
            if (nextTime != now){
                nextTime += period;
            }
            return new Date(nextTime);
        }
        return null;
    }

}
