package com.cumulus.modules.quartz.config;

import com.cumulus.modules.quartz.entity.QuartzJob;
import com.cumulus.modules.quartz.entity.QuartzJobEnum;
import com.cumulus.modules.quartz.repository.QuartzJobRepository;
import com.cumulus.modules.quartz.utils.QuartzManage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 定时任务的中断处理服务
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class JobRunner implements ApplicationRunner {

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

    /**
     * 默认启动的任务类型
     */
    public static final List<String> DEFAULT_START_JOB_TYPE = Arrays.asList(QuartzJobEnum.LOG_FILE_ARCHIVE.getJobType(),
            QuartzJobEnum.VUL_SCAN_RETEST.getJobType(), QuartzJobEnum.SAVE_VULNERABILITY_COUNT.getJobType());

    @Override
    public void run(ApplicationArguments applicationArguments) {
        if (log.isInfoEnabled()) {
            log.info("Start inject scheduled tasks");
        }
        // 中断处理
        List<QuartzJob> defaultJob = quartzJobRepository.findAllByJobTypeInAndIsPauseIsTrue(DEFAULT_START_JOB_TYPE);
        for (QuartzJob job : defaultJob) {
            job.setIsPause(false);
        }
        quartzJobRepository.saveAll(defaultJob);
        List<QuartzJob> quartzJobs = quartzJobRepository.findByIsPauseIsFalse();
        quartzJobs.forEach(quartzManage::addJob);
        if (log.isInfoEnabled()) {
            log.info("Scheduled tasks injection completed");
        }
    }

}
