package com.cumulus.modules.business.gather.request;


import lombok.Getter;
import lombok.Setter;

/**
 * 对采集引擎响应的一层包裹
 *
 * @author zhaoff
 */
@Getter
@Setter
public class TaskResponseWrapper {

    /**
     * 引擎返回的采集响应对象
     */
    private TaskResponse taskResponse;

}
