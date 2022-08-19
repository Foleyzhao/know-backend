package com.cumulus.modules.quartz.controller;

import com.cumulus.entity.LogFile;
import com.cumulus.modules.quartz.entity.QuartzJob;
import com.cumulus.modules.quartz.service.QuartzJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 用于其他包无法调用quartz服务 时使用 如logging包 的定时归档任务
 *
 * @author : shenjc
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quartz")
public class QuartzJobController {

    /**
     * 定时任务服务
     */
    @Autowired
    private QuartzJobService quartzJobService;

    /**
     * 获取日志归档任务间隔
     *
     * @param day 时间 单位 日
     * @return 返回状态 OK 200
     */
    @PutMapping(value = "/updateLogFileArchive")
    public ResponseEntity<Object> updateLogFileArchive(Integer day) {
        QuartzJob job = quartzJobService.findById(LogFile.QUARTZ_JOB_ID);
        job.setPeriod(TimeUnit.DAYS.toSeconds(day));
        quartzJobService.update(job);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 获取日志归档任务间隔
     *
     * @return 单个时间 单位 日
     */
    @GetMapping(value = "/getLogFileArchive")
    public ResponseEntity<Object> getLogFileArchive() {
        QuartzJob job = quartzJobService.findById(LogFile.QUARTZ_JOB_ID);
        return new ResponseEntity<>(TimeUnit.SECONDS.toDays(job.getPeriod()), HttpStatus.OK);
    }
}
