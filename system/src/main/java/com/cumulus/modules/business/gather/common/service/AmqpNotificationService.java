package com.cumulus.modules.business.gather.common.service;

/**
 * 内部通知使用的服接口
 *
 * @author zhaoff
 */
public interface AmqpNotificationService {

    /**
     * MQ路由键-采集资产更新消息
     */
    String MSG_UPDATE_FINISHED = "gatherAsset.updateFinished";

    /**
     * MQ路由键-采集资产更新消息内容键-采集资产ID
     */
    String MSG_UPDATE_FINISHED_CONTENT_GATHERASSETID = "gatherAssetId";

    /**
     * MQ路由键-采集资产更新消息内容键-资产采集日志
     */
    String MSG_UPDATE_FINISHED_CONTENT_ASSETLOGID = "assetLog";

    /**
     * MQ路由键-采集资产更新消息内容键-资产ID
     */
    String MSG_UPDATE_FINISHED_CONTENT_ASSETID = "assetId";

    /**
     * MQ路由键-采集任务完成消息
     */
    String MSG_GATHER_JOB_FINISHED = "gatherjob.finished";

    /**
     * MQ路由键-采集任务完成消息内容键-采集任务ID
     */
    String MSG_GATHER_JOB_FINISHED_CONTENT_LOGID = "logId";

    /**
     * MQ路由键-采集任务完成消息内容键-开始时间
     */
    String MSG_GATHER_JOB_FINISHED_CONTENT_STARTTIME = "start";

    /**
     * MQ路由键-采集任务完成消息内容键-结束时间
     */
    String MSG_GATHER_JOB_FINISHED_CONTENT_ENDTIME = "end";

    /**
     * MQ路由键-采集任务完成消息内容键-资产ID列表
     */
    String MSG_GATHER_JOB_FINISHED_CONTENT_ASSETIDS = "assetIds";

    /**
     * 发送通知消息
     *
     * @param routingKey 消息的路由键值
     * @param entity     消息实体
     */
    void sendNotification(String routingKey, Object entity);


    /**
     * 手工增加一个 ChangeListener
     *
     * @param changeListener 新增加的 ChangeListener
     */
    void addChangeListener(ChangeListener changeListener);

}
