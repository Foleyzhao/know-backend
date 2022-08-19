package com.cumulus.utils;

/**
 * 回调任务接口
 */
public interface CallBack {

    /**
     * 回调执行方法
     */
    void executor();

    /**
     * 回调任务名称
     *
     * @return 回调任务名称
     */
    default String getCallBackName() {
        return Thread.currentThread().getId() + ":" + this.getClass().getName();
    }

}

