package com.cumulus.modules.business.utils;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 构建cron表达式模型
 *
 * @author zhangxq
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskScheduleModel {

    /**
     * 所选作业类型:
     * 1  -> 每天
     * 2  -> 每月
     * 3  -> 每周
     */
    Integer jobType;

    /**
     * 一周的哪几天
     */
    List<Integer> dayOfWeeks;

    /**
     * 一个月的哪几天
     */
    List<Integer> dayOfMonths;

    /**
     * 秒
     */
    Integer second;

    /**
     * 分
     */
    Integer minute;

    /**
     * 时
     */
    Integer hour;

    public static final int WEEKLY = 3;

    public static final int MONTHLY = 2;
}
