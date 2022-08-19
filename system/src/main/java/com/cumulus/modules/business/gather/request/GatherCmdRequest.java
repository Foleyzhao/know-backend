package com.cumulus.modules.business.gather.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 采集项采集请求对象
 *
 * @author zhaoff
 */
@Getter
@Setter
public class GatherCmdRequest {

    /**
     * 采集项key
     */
    private String key;

    /**
     * 采集结果变量
     */
    private Map<String, Object> vars;

    /**
     * 采集的原始输出
     * {
     *     output：原始输出
     * }
     */
    private Map<String, Object> outputs;

    /**
     * 采集是否成功
     */
    private boolean success = true;

    /**
     * 采集失败的原因
     */
    private String errorMsg;

    /**
     * 超时等待时间
     */
    private Integer tto;

    /**
     * 构造函数
     *
     * @param key 采集项的key
     */
    public GatherCmdRequest(String key) {
        this.key = key;
    }

}
