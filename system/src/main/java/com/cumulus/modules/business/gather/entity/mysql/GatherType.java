package com.cumulus.modules.business.gather.entity.mysql;

import lombok.Getter;
import lombok.Setter;

/**
 * 资产类型状态
 *
 * @author shijh
 */
@Getter
@Setter
public class GatherType {

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 状态 0-开始 1-结束 2-执行中
     */
    private Integer status;

    /**
     * 结果 0-成功 1-失败
     */
    private Integer result;

    /**
     * 消息
     */
    private String message;
}
