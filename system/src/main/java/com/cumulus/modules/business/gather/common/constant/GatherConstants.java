package com.cumulus.modules.business.gather.common.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 采集相关常量
 *
 * @author zhaoff
 */
public class GatherConstants {

    /**
     * 资产类型常量：1 - 主机
     */
    public static final Integer TYPE_HOST = 1;

    /**
     * 资产类型常量：2 - 网络资产
     */
    public static final Integer TYPE_NETWORK = 2;

    /**
     * 资产类型常量：3 - 安全设备
     */
    public static final Integer TYPE_SECURITY = 3;

    /**
     * 资产类型常量：4 - 数据库
     */
    public static final Integer TYPE_DATABASE = 4;

    /**
     * 资产类型常量：5 -应用
     */
    public static final Integer TYPE_APPLICATION = 5;

    /**
     * 资产类型常量： 6 - 中间件
     */
    public static final Integer TYPE_MIDDLEWARE = 6;

    /**
     * 资产类型常量： 7 - C/S资产
     */
    public static final Integer TYPE_CS_ASSET = 7;

    /**
     * 资产类型常量： 8 - B/S资产
     */
    public static final Integer TYPE_BS_ASSET = 8;

    /**
     * 资产系统类型常量： 1 - Windows server
     */
    public static final Integer TYPE_SYS_WINDOWS_SERVICE = 1;

    /**
     * 资产系统类型常量： 2 - Centos/Redhat
     */
    public static final Integer TYPE_SYS_CENTOS_REDHAT = 2;

    /**
     * 资产系统类型常量： 3 - HP-UX
     */
    public static final Integer TYPE_SYS_HP_UX = 3;

    /**
     * 资产系统类型常量： 4 - Debian
     */
    public static final Integer TYPE_SYS_DEBIAN = 4;

    /**
     * 资产系统类型常量： 5 - SUSE
     */
    public static final Integer TYPE_SYS_SUSE = 5;

    /**
     * 资产系统类型常量： 6 - Solaris
     */
    public static final Integer TYPE_SYS_SOLARIS = 6;

    /**
     * 资产系统类型常量： 7 - vmware esxi
     */
    public static final Integer TYPE_SYS_VMWARE = 7;

    /**
     * 资产系统类型常量： 8 - H3C(Comware)
     */
    public static final Integer TYPE_SYS_H3C_COMWARE = 8;

    /**
     * 资产系统类型常量： 9 - Huawei(VRP)
     */
    public static final Integer TYPE_SYS_HUAWEI_VRP = 9;

    /**
     * 资产系统类型常量： 10 - Cisco(IOS)
     */
    public static final Integer TYPE_SYS_CISCO_IOS = 10;

    /**
     * 资产系统类型常量： 11 - ZTE
     */
    public static final Integer TYPE_SYS_ZTE = 11;

    /**
     * 资产系统类型常量： 12 - H3C(Comware)安全设备
     */
    public static final Integer TYPE_SYS_SECURITY_H3C_COMWARE = 12;

    /**
     * 资产系统类型常量： 13 - Huawei(VRP)安全设备
     */
    public static final Integer TYPE_SYS_SECURITY_HUAWEI_VRP = 13;

    /**
     * 资产系统类型常量： 14 - Juniper(Junos)
     */
    public static final Integer TYPE_SYS_JUNIPER = 14;

    /**
     * 资产系统类型常量： 15 - MySql
     */
    public static final Integer TYPE_SYS_MYSQL = 15;

    /**
     * 资产系统类型常量： 16 - MSSQL
     */
    public static final Integer TYPE_SYS_MSSQL = 16;

    /**
     * 资产系统类型常量： 17 - Oracle
     */
    public static final Integer TYPE_SYS_ORACLE = 17;

    /**
     * 资产系统类型常量： 18 - Postgresql
     */
    public static final Integer TYPE_SYS_POSTGRESQL = 18;

    /**
     * 资产系统类型常量： 19 - apache
     */
    public static final Integer TYPE_SYS_APACHE = 19;

    /**
     * 资产系统类型常量： 20 - nginx
     */
    public static final Integer TYPE_SYS_NGINX = 20;

    /**
     * 资产系统类型常量： 21 - weblogic
     */
    public static final Integer TYPE_SYS_WEBLOGIC = 21;

    /**
     * 资产系统类型常量： 22 - tomcat
     */
    public static final Integer TYPE_SYS_TOMCAT = 22;

    /**
     * 资产系统类型常量： 23 - Navicat Premium
     */
    public static final Integer TYPE_SYS_NAVICAT = 23;

    /**
     * 资产系统类型常量： 24 - HRM
     */
    public static final Integer TYPE_SYS_HRM = 24;

    /**
     * 资产系统类型常量： 25 - CRM
     */
    public static final Integer TYPE_SYS_CRM = 25;

    /**
     * 资产系统类型常量： 26 - OA
     */
    public static final Integer TYPE_SYS_OA = 26;

    /**
     * 资产类型字符串常量：1 - 主机
     */
    public static final String TYPE_HOST_STR = "1";

    /**
     * 资产类型字符串常量：2 - 网络资产
     */
    public static final String TYPE_NETWORK_STR = "2";

    /**
     * 资产类型字符串常量：3 - 安全设备
     */
    public static final String TYPE_SECURITY_STR = "3";

    /**
     * 资产类型字符串常量：4 - 数据库
     */
    public static final String TYPE_DATABASE_STR = "4";

    /**
     * 资产类型字符串常量：5 -应用
     */
    public static final String TYPE_APPLICATION_STR = "5";

    /**
     * 资产类型字符串常量： 6 - 中间件
     */
    public static final String TYPE_MIDDLEWARE_STR = "6";

    /**
     * 资产类型字符串常量： 7 - C/S资产
     */
    public static final String TYPE_CS_ASSET_STR = "7";

    /**
     * 资产类型字符串常量： 8 - B/S资产
     */
    public static final String TYPE_BS_ASSET_STR = "8";

    /**
     * 资产系统类型字符串常量： 1 - Windows server
     */
    public static final String TYPE_SYS_WINDOWS_SERVICE_STR = "1";

    /**
     * 资产系统类型常量： 2 - Centos/Redhat
     */
    public static final String TYPE_SYS_CENTOS_REDHAT_STR = "2";

    /**
     * 资产系统类型字符串常量： 3 - HP-UX
     */
    public static final String TYPE_SYS_HP_UX_STR = "3";

    /**
     * 资产系统类型字符串常量： 4 - Debian
     */
    public static final String TYPE_SYS_DEBIAN_STR = "4";

    /**
     * 资产系统类型字符串常量： 5 - SUSE
     */
    public static final String TYPE_SYS_SUSE_STR = "5";

    /**
     * 资产系统类型字符串常量： 6 - Solaris
     */
    public static final String TYPE_SYS_SOLARIS_STR = "6";

    /**
     * 资产系统类型字符串常量： 7 - vmware esxi
     */
    public static final String TYPE_SYS_VMWARE_STR = "7";

    /**
     * 资产系统类型字符串常量： 8 - H3C(Comware)
     */
    public static final String TYPE_SYS_H3C_COMWARE_STR = "8";

    /**
     * 资产系统类型字符串常量： 9 - Huawei(VRP)
     */
    public static final String TYPE_SYS_HUAWEI_VRP_STR = "9";

    /**
     * 资产系统类型字符串常量： 10 - Cisco(IOS)
     */
    public static final String TYPE_SYS_CISCO_IOS_STR = "10";

    /**
     * 资产系统类型字符串常量： 11 - ZTE
     */
    public static final String TYPE_SYS_ZTE_STR = "11";

    /**
     * 资产系统类型字符串常量： 12 - H3C(Comware)安全设备
     */
    public static final String TYPE_SYS_SECURITY_H3C_COMWARE_STR = "12";

    /**
     * 资产系统类型字符串常量： 13 - Huawei(VRP)安全设备
     */
    public static final String TYPE_SYS_SECURITY_HUAWEI_VRP_STR = "13";

    /**
     * 资产系统类型字符串常量： 14 - Juniper(Junos)
     */
    public static final String TYPE_SYS_JUNIPER_STR = "14";

    /**
     * 资产系统类型字符串常量： 15 - MySql
     */
    public static final String TYPE_SYS_MYSQL_STR = "15";

    /**
     * 资产系统类型字符串常量： 16 - MSSQL
     */
    public static final String TYPE_SYS_MSSQL_STR = "16";

    /**
     * 资产系统类型字符串常量： 17 - Oracle
     */
    public static final String TYPE_SYS_ORACLE_STR = "17";

    /**
     * 资产系统类型字符串常量： 18 - Postgresql
     */
    public static final String TYPE_SYS_POSTGRESQL_STR = "18";

    /**
     * 资产系统类型字符串常量： 19 - apache
     */
    public static final String TYPE_SYS_APACHE_STR = "19";

    /**
     * 资产系统类型字符串常量： 20 - nginx
     */
    public static final String TYPE_SYS_NGINX_STR = "20";

    /**
     * 资产系统类型字符串常量： 21 - weblogic
     */
    public static final String TYPE_SYS_WEBLOGIC_STR = "21";

    /**
     * 资产系统类型字符串常量： 22 - tomcat
     */
    public static final String TYPE_SYS_TOMCAT_STR = "22";

    /**
     * 资产系统类型字符串常量： 23 - Navicat Premium
     */
    public static final String TYPE_SYS_NAVICAT_STR = "23";

    /**
     * 资产系统类型字符串常量： 24 - HRM
     */
    public static final String TYPE_SYS_HRM_STR = "24";

    /**
     * 资产系统类型字符串常量： 25 - CRM
     */
    public static final String TYPE_SYS_CRM_STR = "25";

    /**
     * 资产系统类型字符串常量： 26 - OA
     */
    public static final String TYPE_SYS_OA_STR = "26";


    /**
     * 采集状态： 0 - 禁用
     */
    public static final Integer GATHER_STATE_FORBIDDEN = 0;

    /**
     * 采集状态： 1 - 无法访问
     */
    public static final Integer GATHER_STATE_NOTACCESS = 1;

    /**
     * 采集状态： 2 - 未采集
     */
    public static final Integer GATHERSTATE_NOTCOLLECTION = 2;

    /**
     * 采集状态： 3 - 未核查
     */
    public static final Integer GATHERSTATE_UNVERIFIED = 3;

    /**
     * 采集状态： 4 - 不合规
     */
    public static final Integer GATHERSTATE_NOTCOMPLIANT = 4;

    /**
     * 采集状态： 5 - 部分采集/未核查
     */
    public static final Integer GATHERSTATE_NOTCOLLECTION_UNVERIFIED = 5;

    /**
     * 采集状态： 6 - 部分采集/不合规
     */
    public static final Integer GATHERSTATE_NOTCOLLECTION_NOTCOMPLIANT = 6;

    /**
     * 采集状态： 7 - 部分采集/合规
     */
    public static final Integer GATHERSTATE_NOTCOLLECTION_COMPLIANT = 7;

    /**
     * 采集状态： 8 - 合规
     */
    public static final Integer GATHERSTATE_COMPLIANT = 8;

    /**
     * 采集类型--实时项
     */
    public static final Integer TYPE_FREQUENTLY_ITEM = 0;

    /**
     * 采集类型--耗时项
     */
    public static final Integer TYPE_STATIONARY_ITEM = 1;

    /**
     * 采集类型--不常变化项
     */
    public static final Integer TYPE_SELDOM_ITEM = 2;

    /**
     * 采集类型--实时项字符串
     */
    public static final String TYPE_FREQUENTLY_ITEM_STR = "frequently";

    /**
     * 采集类型--耗时项字符串
     */
    public static final String TYPE_STATIONARY_ITEM_STR = "stationary";

    /**
     * 采集类型--不常变化项字符串
     */
    public static final String TYPE_SELDOM_ITEM_STR = "seldom";

    /**
     * 周期采集单位-月
     */
    public static final String SCHEDULE_GATHER_UNIT_MOUTH = "month";

    /**
     * 周期采集单位-日
     */
    public static final String SCHEDULE_GATHER_UNIT_DAY = "day";

    /**
     * 周期采集单位-小时
     */
    public static final String SCHEDULE_GATHER_UNIT_HOUR = "hour";

    /**
     * 周期采集单位-分钟
     */
    public static final String SCHEDULE_GATHER_UNIT_MINUTE = "minute";

    /**
     * 周期采集单位-单次
     */
    public static final String SCHEDULE_GATHER_UNIT_ONCE = "once";

    /**
     * 连接协议：ssh
     */
    public static final String PROTO_SSH = "ssh";

    /**
     * 连接协议：telnet
     */
    public static final String PROTO_TELNET = "telnet";

    /**
     * 连接协议：winrm
     */
    public static final String PROTO_WINRM = "winrm";


    /**
     * 执行方式：1-手动执行
     */
    public static final Integer EXEC_TYPE_MANUAL = 1;

    /**
     * 执行方式：2-自动执行
     */
    public static final Integer EXEC_TYPE_AUTO = 2;


    /**
     * 采集计划状态： 0 - 未开始
     */
    public static final Integer STATE_UNSTART = 0;

    /**
     * 采集计划状态： 1 - 正在执行
     */
    public static final Integer STATE_RUNNING = 1;

    /**
     * 采集计划状态： 2 - 执行结束
     */
    public static final Integer STATE_END = 2;

    /**
     * 采集计划状态： 3 - 正在取消
     */
    public static final Integer STATE_CANCELLING = 3;

    /**
     * 采集计划状态：4 - 暂停
     */
    public static final Integer STATE_STOP = 4;


    /**
     * 执行结果状态常量：0 成功
     */
    public static final Integer STATE_SUCCESS = 0;

    /**
     * 执行结果状态常量：1 失败
     */
    public static final Integer STATE_FAIL = 1;

    /**
     * 执行结果状态常量：2 部分成功
     */
    public static final Integer STATE_PORTION = 2;

    /**
     * 执行结果状态常量：3 正在进行中
     */
    public static final Integer STATE_PROCESSING = 3;

    /**
     * 执行结果状态常量：4 可终止状态
     */
    public static final Integer STATE_CANCELABLE = 4;

    /**
     * 执行结果状态常量：5 终止中
     */
    public static final Integer STATE_CANCELING = 5;


    /**
     * 数据采集结果：0 成功
     */
    public static final Integer RESULT_SUCCESS = 0;

    /**
     * 数据采集结果：1 失败
     */
    public static final Integer RESULT_FAIL = 1;

    /**
     * 数据采集结果：2 部分成功
     */
    public static final Integer RESULT_PORTION = 2;


    /**
     * 采集成功数量
     */
    public static final String SUCCEED_COUNT = "succeed";

    /**
     * 采集失败数量
     */
    public static final String FAILED_COUNT = "failed";

    /**
     * 部分成功数量
     */
    public static final String PORTION_COUNT = "portion";

    /**
     * 获取一级标题
     *
     * @return 一级标题
     */
    public static List<Integer> getType() {
        return Arrays.asList(
                GatherConstants.TYPE_HOST,
                GatherConstants.TYPE_NETWORK,
                GatherConstants.TYPE_SECURITY,
                GatherConstants.TYPE_DATABASE,
                GatherConstants.TYPE_APPLICATION,
                GatherConstants.TYPE_MIDDLEWARE,
                GatherConstants.TYPE_CS_ASSET,
                GatherConstants.TYPE_BS_ASSET
        );
    }

    // --- 资产状态 ---

    /**
     * 资产状态-未采集
     */
    public static final Integer GATHER_ASSET_STATUS_NOT_COLLECTED = 0;

    /**
     * 资产状态-在线
     */
    public static final Integer GATHER_ASSET_STATUS_ONLINE = 1;

    /**
     * 资产状态-离线
     */
    public static final Integer GATHER_ASSET_STATUS_OFFLINE = 2;

    // --- 风险等级 ---

    /**
     * 风险等级-未采集
     */
    public static final Integer RISK_LEVEL_NOT_COLLECTED = -1;


    // --- 采集引擎属性key ---

    /**
     * 采集引擎属性key-ID
     */
    public static final String GATHER_RESPONSE_ID = "id";

    /**
     * 采集引擎属性key-执行结果
     */
    public static final String GATHER_RESPONSE_CODE = "code";

    /**
     * 采集引擎属性key-系统编码
     */
    public static final String GATHER_RESPONSE_ENCODING = "encoding";

    /**
     * 采集引擎属性key-标准输出流
     */
    public static final String GATHER_RESPONSE_STDOUT = "stdout";

    /**
     * 采集引擎属性key-执行指令
     */
    public static final String GATHER_RESPONSE_CMD = "cmd";

    /**
     * 采集引擎属性key-错误码
     */
    public static final String GATHER_RESPONSE_RETURN_CODE = "returncode";

    /**
     * 采集引擎属性key-错误输出流
     */
    public static final String GATHER_RESPONSE_STDERR = "stderr";

    /**
     * 采集引擎属性key-指令执行开始时间
     */
    public static final String GATHER_RESPONSE_START_TIME = "start_time";

    /**
     * 采集引擎属性key-指令执行结束时间
     */
    public static final String GATHER_RESPONSE_END_TIME = "end_time";

    /**
     * 采集引擎属性key-错误信息
     */
    public static final String GATHER_RESPONSE_ERR_INFO = "err_info";

    // --- 采集信息ES索引 ---

    /**
     * es索引名-资产采集日志
     */
    public static final String ES_INDEX_ASSET_LOG = "gather-asset-log";

    /**
     * es索引名-基本信息
     */
    public static final String ES_INDEX_BASIC_INFO = "basic-info";

    /**
     * es索引名-硬件信息
     */
    public static final String ES_INDEX_HARDWARE_INFO = "hardware";

    /**
     * es索引名-磁盘分区
     */
    public static final String ES_INDEX_DISK = "disk-partition";

    /**
     * es索引名-帐号信息
     */
    public static final String ES_INDEX_ACCOUNT = "account";

    /**
     * es索引名-安全配置
     */
    public static final String ES_INDEX_SEC_CONFIG = "security-config";

    /**
     * es索引名-网络配置
     */
    public static final String ES_INDEX_NET_CONFIG = "network";

    /**
     * es索引名-服务
     */
    public static final String ES_INDEX_SERVICE = "service";

    /**
     * es索引名-路由表
     */
    public static final String ES_INDEX_ROUTE = "route";

    /**
     * es索引名-环境变量
     */
    public static final String ES_INDEX_ENVIRONMENT = "environment";

    /**
     * es索引名-端口
     */
    public static final String ES_INDEX_NETSTAT = "port";

    /**
     * es索引名-已装软件
     */
    public static final String ES_INDEX_SW = "software";

    /**
     * es索引名-系统进程
     */
    public static final String ES_INDEX_PROCESS = "`system-processes`";

    /**
     * es索引名-性能
     */
    public static final String ES_INDEX_PERFORMANCE = "performance";

    /**
     * es索引名-敏感文件
     */
    public static final String ES_INDEX_SENSITIVE_FILE = "sensitive-file";

    /**
     * es索引名-内核更新
     */
    public static final String ES_INDEX_KERNEL_UPDATE = "kernel-update";

    // --- 采集项 itemKey ---

    /**
     * itemKey-预采集
     */
    public static final String ITEM_KEY_PRE_REQUISITE = "_prerequisite";

    /**
     * itemKey-厂商
     */
    public static final String ITEM_KEY_VENDOR = "hardware.hardware.vendor";

    /**
     * itemKey-硬件型号
     */
    public static final String ITEM_KEY_SYSTEM_MODEL = "hardware.hardware.systemmodel";

    /**
     * itemKey-CPU型号
     */
    public static final String ITEM_KEY_CPU_MODEL = "hardware.hardware.cpumodel";

    /**
     * itemKey-CPU核数
     */
    public static final String ITEM_KEY_CPU_CORES = "hardware.hardware.cpucores";

    /**
     * itemKey-内存大小
     */
    public static final String ITEM_KEY_PHYSICAL_MEMORY = "hardware.hardware.physicalmemory";

    /**
     * itemKey-硬盘大小
     */
    public static final String ITEM_KEY_DISK_SIZE = "hardware.hardware.disksize";

    /**
     * itemKey-上线时间
     */
    public static final String ITEM_KEY_RUNTIME = "hardware.hardware.runtime";

    /**
     * itemKey-UUID
     */
    public static final String ITEM_KEY_UUID = "hardware.hardware.uuid";

    /**
     * itemKey-电源数量
     */
    public static final String ITEM_KEY_POWER_NUM = "hardware.hardware.psnum";

    /**
     * itemKey-电源功率
     */
    public static final String ITEM_KEY_POWER_SUPPLY = "hardware.hardware.powersupply";

    /**
     * itemKey-CPU主频
     */
    public static final String ITEM_KEY_CPU_FREQUENCY = "hardware.hardware.cpufrequency";

    /**
     * itemKey-内存数量
     */
    public static final String ITEM_KEY_MEMORY_NUM = "hardware.hardware.memorynum";

    /**
     * itemKey-帐号信息
     */
    public static final String ITEM_KEY_ACCOUNT_INFO = "account.accountsinfo";

    /**
     * itemKey-OS版本
     */
    public static final String ITEM_KEY_OS_VERSION = "osversion.osversion.osversion";

    /**
     * itemKey-内核版本
     */
    public static final String ITEM_KEY_KERNEL = "osversion.osversion.kernel";

    /**
     * itemKey-内核更新
     */
    public static final String ITEM_KEY_KERNEL_UPDATE = "osversion.osversion.kernelupdate";

    /**
     * itemKey-OS厂商
     */
    public static final String ITEM_KEY_OS_VENDOR = "osversion.osversion.osvendor";

    /**
     * itemKey-主机名
     */
    public static final String ITEM_KEY_HOSTNAME = "osversion.osversion.hostname";

    /**
     * itemKey-已装软件
     */
    public static final String ITEM_KEY_SOFTWARE = "soft.other.software";

    /**
     * itemKey-非法工具
     */
    public static final String ITEM_KEY_ILLEGAL_TOOLS = "soft.illegaltools";

    /**
     * itemKey-安全配置
     */
    public static final String ITEM_KEY_SECURITY_CFG = "securitycfg";

    /**
     * itemKey-环境变量
     */
    public static final String ITEM_KEY_ENVIRONMENT = "securitycfg.environment";

    /**
     * itemKey-敏感文件
     */
    public static final String ITEM_KEY_SENSITIVE_FILE = "securitycfg.sensitivefile";

    /**
     * itemKey-网卡配置
     */
    public static final String ITEM_KEY_NET_PARAM = "networkcfg.networkcardinfo.netparam";

    /**
     * itemKey-路由表
     */
    public static final String ITEM_KEY_ROUTE = "networkcfg.networkrouteinfo.route";

    /**
     * itemKey-网络状态
     */
    public static final String ITEM_KEY_NET_STATUS = "networkcfg.networkstatus.netstatus";

    /**
     * itemKey-进程信息
     */
    public static final String ITEM_KEY_PROCESS = "process.process.process";

    /**
     * itemKey-服务信息
     */
    public static final String ITEM_KEY_SERVICE = "service.service.service";

    /**
     * itemKey-性能
     */
    public static final String ITEM_KEY_PERFORMANCE = "performance.performance";

    /**
     * itemKey-磁盘分区
     */
    public static final String ITEM_KEY_DISK_PARTITION = "performance.performance.diskpartition";

}
