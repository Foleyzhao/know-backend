package com.cumulus.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author shenjc
 */
public class DateUtils {

    /**
     * 日期格式字符串字符串
     */
    public static final String YYYY_MM_DD_STR = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String HH_MM_SS_STR = "HH:mm:ss";
    public static final String MM_DD_STR = "MM-dd";


    public static final String DAY_STR = "天";
    public static final String HOUR_STR = "小时";
    public static final String MINUTE_STR = "分钟";
    public static final String SEC_STR = "秒";
    public static final String DEFAULT_INTERVAL_TIME = "0秒";

    public static final int SEC_TO_MINUTE = 60;
    public static final int MINUTE_TO_HOUR = 60;
    public static final int HOUR_TO_DAY = 24;
    public static final int MILLISECOND_TO_SEC = 1000;


    /**
     * 日期格式：年-月-日 时:分:秒
     */
    public static final DateTimeFormatter DFY_MD_HMS = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS_STR);

    /**
     * 日期格式：年-月-日
     */
    public static final DateTimeFormatter DFY_MD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 日期格式：时分秒
     */
    public static final DateTimeFormatter DFY_HMS = DateTimeFormatter.ofPattern(HH_MM_SS_STR);

    /**
     * 日期格式：年-月-日 时:分:秒
     */
    public static final SimpleDateFormat SIMPLE_DFY_MD_HMS = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_STR);

    /**
     * 日期格式：年-月-日 时:分:秒
     */
    public static final SimpleDateFormat SIMPLE_DFY_MD = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_STR);

    /**
     * LocalDateTime格式时间转时间戳格式时间
     *
     * @param localDateTime LocalDateTime格式时间
     * @return 时间戳格式时间
     */
    public static Long getTimeStamp(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * 时间戳格式时间转LocalDateTime格式时间
     *
     * @param timeStamp 时间戳格式时间
     * @return LocalDateTime格式时间
     */
    public static LocalDateTime fromTimeStamp(Long timeStamp) {
        return LocalDateTime.ofEpochSecond(timeStamp, 0, OffsetDateTime.now().getOffset());
    }

    /**
     * LocalDateTime格式时间转Date格式时间
     *
     * @param localDateTime LocalDateTime格式时间
     * @return Date格式时间
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate格式时间转Date格式时间
     *
     * @param localDate LocalDate格式时间
     * @return Date格式时间
     */
    public static Date toDate(LocalDate localDate) {
        return toDate(localDate.atTime(LocalTime.now(ZoneId.systemDefault())));
    }

    /**
     * Date格式时间转LocalDateTime格式时间
     *
     * @param date Date格式时间
     * @return LocalDateTime格式时间
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime格式时间格式化
     *
     * @param localDateTime LocalDateTime格式时间
     * @param patten        日期格式
     * @return 格式化日期
     */
    public static String localDateTimeFormat(LocalDateTime localDateTime, String patten) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern(patten);
        return df.format(localDateTime);
    }

    /**
     * LocalDateTime格式时间格式化
     *
     * @param localDateTime LocalDateTime格式时间
     * @param df            日期格式
     * @return 格式化日期
     */
    public static String localDateTimeFormat(LocalDateTime localDateTime, DateTimeFormatter df) {
        return df.format(localDateTime);
    }

    /**
     * LocalDateTime格式时间格式化（yyyy-MM-dd HH:mm:ss）
     *
     * @param localDateTime LocalDateTime格式时间
     * @return 格式化日期
     */
    public static String localDateTimeFormatyMdHms(LocalDateTime localDateTime) {
        return DFY_MD_HMS.format(localDateTime);
    }

    /**
     * LocalDateTime格式时间格式化（yyyy-MM-dd）
     *
     * @param localDateTime LocalDateTime格式时间
     * @return 格式化日期
     */
    public static String localDateTimeFormatyMd(LocalDateTime localDateTime) {
        return DFY_MD.format(localDateTime);
    }

    /**
     * 字符串格式时间转LocalDateTime格式时间
     *
     * @param localDateTime 字符串格式时间
     * @param pattern       日期格式
     * @return LocalDateTime格式时间
     */
    public static LocalDateTime parseLocalDateTimeFormat(String localDateTime, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.from(dateTimeFormatter.parse(localDateTime));
    }

    /**
     * 字符串格式时间转LocalDateTime格式时间
     *
     * @param localDateTime     字符串格式时间
     * @param dateTimeFormatter 日期格式
     * @return LocalDateTime格式时间
     */
    public static LocalDateTime parseLocalDateTimeFormat(String localDateTime, DateTimeFormatter dateTimeFormatter) {
        return LocalDateTime.from(dateTimeFormatter.parse(localDateTime));
    }

    /**
     * 字符串格式时间转LocalDateTime格式时间（字符串格式 yyyy-MM-dd HH:mm:ss）
     *
     * @param localDateTime 字符串格式时间
     * @return LocalDateTime格式时间
     */
    public static LocalDateTime parseLocalDateTimeFormatyMdHms(String localDateTime) {
        return LocalDateTime.from(DFY_MD_HMS.parse(localDateTime));
    }

    /**
     * 秒转换为指定格式的日期
     *
     * @param second 秒值
     * @param patten 日期格式
     * @return 字符串格式
     */
    public static String secondToDate(long second, String patten) {
        Calendar calendar = Calendar.getInstance();
        //转换为毫秒
        calendar.setTimeInMillis(second * 1000);
        Date date = calendar.getTime();
        SimpleDateFormat format = new SimpleDateFormat(patten);
        return format.format(date);
    }

    /**
     * 获取间隔的时间
     *
     * @param timeMax 大的时间(毫秒)
     * @param timeMin 小的时间(毫秒)
     * @return 返回间隔时间 x天x小时...
     */
    public static String getIntervalTime(Long timeMin, Long timeMax) {
        if (timeMax == null || timeMin == null) {
            return DEFAULT_INTERVAL_TIME;
        }
        if (timeMax.equals(timeMin)) {
            return DEFAULT_INTERVAL_TIME;
        }
        if (timeMax < timeMin) {
            long media = timeMax;
            timeMax = timeMin;
            timeMin = media;
        }
        long intervalTime = timeMax - timeMin;
        intervalTime = intervalTime / MILLISECOND_TO_SEC;
        StringBuilder stringBuilder = new StringBuilder();
        if (intervalTime > 0) {
            long sec = intervalTime % SEC_TO_MINUTE;
            if (sec > 0) {
                stringBuilder.insert(0, SEC_STR).insert(0, sec);
            }
        }
        intervalTime = intervalTime / SEC_TO_MINUTE;
        if (intervalTime > 0) {
            long minute = intervalTime % MINUTE_TO_HOUR;
            if (minute > 0) {
                stringBuilder.insert(0, MINUTE_STR).insert(0, minute);
            }
        }
        intervalTime = intervalTime / MINUTE_TO_HOUR;
        if (intervalTime > 0) {
            long hour = intervalTime % HOUR_TO_DAY;
            if (hour > 0) {
                stringBuilder.insert(0, HOUR_STR).insert(0, hour);
            }
        }
        intervalTime = intervalTime / HOUR_TO_DAY;
        if (intervalTime > 0) {
            stringBuilder.insert(0, DAY_STR).insert(0, intervalTime);
        }
        if (stringBuilder.length() == 0) {
            return DEFAULT_INTERVAL_TIME;
        }
        return stringBuilder.toString();
    }

    /**
     * 日历对象设置一天的最后一秒(精确到秒)
     *
     * @param calendar 日历对象
     */
    public static void setDayEndSec(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
