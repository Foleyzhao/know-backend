package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * 引擎采集任务请求
 *
 * @author zhaoff
 */
@Getter
@Setter
public class TaskRequest implements Serializable {

    private static final long serialVersionUID = -4366869535435720366L;

    /**
     * 任务类型-采集
     */
    public final static String TYPE_GATHER = "gather";

    /**
     * 执行模式-异步
     */
    public final static String MODE_ASYNC = "async";

    /**
     * 指令执行超时时间（秒）
     */
    public final static Integer EXEC_TTO = 180;

    /**
     * MQ消息存活时间（毫秒）
     */
    public final static String MQ_TTL = "180000";

    /**
     * 唯一标识
     */
    private UUID id;

    /**
     * 任务类型（gather）
     */
    private String type;

    /**
     * 连接信息
     */
    private ConnectionInfo conn;

    /**
     * 帐号信息
     */
    private AccountInfo account;

    /**
     * 执行模式（async-异步）
     */
    private String mode;

    /**
     * 操作超时时间，单位秒
     */
    private Integer tto;

    /**
     * 任务信息
     */
    private TaskInfo task;

    /**
     * 消息自身的TTL(ms)，过期后将从队列中删除
     */
    private String expiration;

}
