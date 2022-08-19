package com.cumulus.modules.business.utils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;

/**
 * cron工具类
 *
 * @author zhangxq
 */
@Slf4j
public class CronUtil {

    /**
     * 构建Cron表达式
     *
     * @param taskScheduleModel
     * @return String
     */
    public static String createCronExpression(TaskScheduleModel taskScheduleModel) {
        StringBuilder cronExp = new StringBuilder("");
        if (null == taskScheduleModel.getJobType()) {
            log.info("执行周期未配置");
        }
        //每隔几秒
        if (null != taskScheduleModel.getSecond()
                && null == taskScheduleModel.getMinute()
                && null == taskScheduleModel.getHour()
                && taskScheduleModel.getJobType() == 0) {
            cronExp.append("0/").append(taskScheduleModel.getSecond());
            cronExp.append(" ");
            cronExp.append("* ");
            cronExp.append("* ");
            cronExp.append("* ");
            cronExp.append("* ");
            cronExp.append("?");
        }
        //每隔几分钟
        if (null != taskScheduleModel.getSecond()
                && null != taskScheduleModel.getMinute()
                && null == taskScheduleModel.getHour()
                && taskScheduleModel.getJobType() == 4) {
            cronExp.append("* ");
            cronExp.append("0/").append(taskScheduleModel.getMinute());
            cronExp.append(" ");
            cronExp.append("* ");
            cronExp.append("* ");
            cronExp.append("* ");
            cronExp.append("?");
        }
        if (null != taskScheduleModel.getSecond()
                && null != taskScheduleModel.getMinute()
                && null != taskScheduleModel.getHour()) {
            //秒
            cronExp.append(taskScheduleModel.getSecond()).append(" ");
            //分
            cronExp.append(taskScheduleModel.getMinute()).append(" ");
            //小时
            cronExp.append(taskScheduleModel.getHour()).append(" ");
            //每天
            if (taskScheduleModel.getJobType() == 1) {
                //日
                cronExp.append("* ");
                //月
                cronExp.append("* ");
                //周
                cronExp.append("?");
            }
            //按每周
            else if (taskScheduleModel.getJobType() == TaskScheduleModel.WEEKLY) {
                //一个月中第几天
                cronExp.append("? ");
                //月份
                cronExp.append("* ");
                //周
                List<Integer> dayOfWeeks = taskScheduleModel.getDayOfWeeks();
                for (Integer dayOfWeek : dayOfWeeks) {
                    if (dayOfWeeks.indexOf(dayOfWeek) == 0) {
                        cronExp.append(dayOfWeek);
                    } else {
                        cronExp.append(",").append(dayOfWeek);
                    }
                }
            }
            //按每月
            else if (taskScheduleModel.getJobType() == TaskScheduleModel.MONTHLY) {
                //一个月中的哪几天
                List<Integer> dayOfMonths = taskScheduleModel.getDayOfMonths();
                for (Integer dayOfMonth : dayOfMonths) {
                    if (dayOfMonths.indexOf(dayOfMonth) == 0) {
                        cronExp.append(dayOfMonth);
                    } else {
                        cronExp.append(",").append(dayOfMonth);
                    }
                }
                //月份
                cronExp.append(" * ");
                //周
                cronExp.append("?");
            }
        } else {
            log.info("时或分或秒参数未配置");//时或分或秒参数未配置
        }
        return cronExp.toString();
    }

    /**
     * 方法摘要：生成计划的详细描述
     *
     * @param taskScheduleModel
     * @return String
     */
    public static String createDescription(TaskScheduleModel taskScheduleModel) {
        StringBuilder description = new StringBuilder("");
        if (null != taskScheduleModel.getSecond()
                && null != taskScheduleModel.getMinute()
                && null != taskScheduleModel.getHour()) {
            //按每天
            if (taskScheduleModel.getJobType() == 1) {
                description.append("每天");
                description.append(taskScheduleModel.getHour()).append("时");
                description.append(taskScheduleModel.getMinute()).append("分");
                description.append(taskScheduleModel.getSecond()).append("秒");
                description.append("执行");
            }
            //按每周
            else if (taskScheduleModel.getJobType() == 3) {
                if (taskScheduleModel.getDayOfWeeks() != null && !taskScheduleModel.getDayOfWeeks().isEmpty()) {
                    String days = "";
                    for (int i : taskScheduleModel.getDayOfWeeks()) {
                        i--;
                        if (i == 0) {
                            i = 7;
                        }
                        days += "周" + i;
                    }
                    description.append("每周的").append(days).append(" ");
                }
                if (null != taskScheduleModel.getSecond()
                        && null != taskScheduleModel.getMinute()
                        && null != taskScheduleModel.getHour()) {
                    description.append(",");
                    description.append(taskScheduleModel.getHour()).append("时");
                    description.append(taskScheduleModel.getMinute()).append("分");
                    description.append(taskScheduleModel.getSecond()).append("秒");
                }
                description.append("执行");
            }
            //按每月
            else if (taskScheduleModel.getJobType() == 2) {
                //选择月份
                if (taskScheduleModel.getDayOfMonths() != null && !taskScheduleModel.getDayOfMonths().isEmpty()) {
                    String days = "";
                    for (int i : taskScheduleModel.getDayOfMonths()) {
                        days += i + "号";
                    }
                    description.append("每月的").append(days).append(" ");
                }
                description.append(taskScheduleModel.getHour()).append("时");
                description.append(taskScheduleModel.getMinute()).append("分");
                description.append(taskScheduleModel.getSecond()).append("秒");
                description.append("执行");
            }
        }
        return description.toString();
    }

    /**
     * 获取下次执行时间
     *
     * @param cron cron表达式
     * @return 下次时间
     */
    public static Timestamp getNextTime(String cron) {
        Timestamp timestamp = null;
        try {
            timestamp = Timestamp.from(new CronExpression(cron).getNextValidTimeAfter(new Date()).toInstant());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}
