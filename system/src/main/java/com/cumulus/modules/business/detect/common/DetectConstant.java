package com.cumulus.modules.business.detect.common;

/**
 * 发现模板常量
 *
 * @author zhangxq
 */
public class DetectConstant {

    /**
     * 引擎返回type ip
     */
    public static final String RESPONSE_TYPE_IP = "ip";

    /**
     * 引擎返回type web
     */
    public static final String RESPONSE_TYPE_WEB = "web";

    /**
     * redisKey 发现任务
     */
    public static final String REDIS_KEY_DETECT = "detect:";

    /**
     * redisKey ip总数
     */
    public static final String REDIS_KEY_NUM = "num";

    /**
     * redisKey ip返回处理数
     */
    public static final String REDIS_KEY_DONE = "done";

    /**
     * redisKey 新增ip数
     */
    public static final String REDIS_KEY_RESULT = "result";

    /**
     * redisKey 在线数
     */
    public static final String REDIS_KEY_ONLINE = "online";

    /**
     * redisKey 下线数
     */
    public static final String REDIS_KEY_OFFLINE = "offline";

    /**
     * redisKey 任务记录id
     */
    public static final String REDIS_KEY_RECORDID = "recordId";

    /**
     * 系统类型 Linux
     */
    public static final String OS_TYPE_LINUX = "Linux";

    /**
     * 系统类型 Windows
     */
    public static final String OS_TYPE_WINDOWS = "Windows";

    /**
     * 系统类型 iOS
     */
    public static final String OS_TYPE_IOS = "iOS";

    /**
     * 端口范围 全部
     */
    public static final int PORT_ALL = 1;

    /**
     * 端口范围 常见top
     */
    public static final int PORT_TOP = 2;

    /**
     * 端口范围 自定义
     */
    public static final int PORT_DIY = 3;

    /**
     * ip范围 全部
     */
    public static final int IP_ALL = 1;

    /**
     * ip范围 按部门
     */
    public static final int IP_DEPT = 2;

    /**
     * ip范围 自定义
     */
    public static final int IP_DIY = 3;

    /**
     * 任务类型 手动-1
     */
    public static final int TASK_TYPE_MANUAL = 1;

    /**
     * 任务类型 每周-2
     */
    public static final int TASK_TYPE_WEEKLY = 2;

    /**
     * 任务类型 每月-3
     */
    public static final int TASK_TYPE_MONTHLY = 3;

    /**
     * 任务类型 自定义-4
     */
    public static final int TASK_TYPE_DIY = 4;

    /**
     * 任务状态 未开始
     */
    public static final int TASK_STATUS_NONE = 0;

    /**
     * 任务状态 执行中
     */
    public static final int TASK_STATUS_RUN = 1;

    /**
     * 任务状态 暂停
     */
    public static final int TASK_STATUS_PAUSE = 2;

    /**
     * 任务状态 结束
     */
    public static final int TASK_STATUS_END = 3;

    /**
     * 采集方式 1-agent
     */
    public static final int GATHER_TYPE_AGENT = 1;

    /**
     * 采集方式 2-登录采集
     */
    public static final int GATHER_TYPE_LOGIN = 2;

    /**
     * 采集方式 3-远程扫描
     */
    public static final int GATHER_TYPE_SCAN = 3;

    /**
     * 进度 分隔符
     */
    public static final String SEPARATOR = "/";

    /**
     * ssh
     */
    public static final String PROTOCOL_SSH = "ssh";

    /**
     * telnet
     */
    public static final String PROTOCOL_TELNET = "telnet";

    /**
     * winrm
     */
    public static final String PROTOCOL_WINRM = "winrm";

}
