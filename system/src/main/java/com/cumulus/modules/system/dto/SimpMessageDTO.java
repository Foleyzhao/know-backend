package com.cumulus.modules.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author : shenjc
 */
@Data
public class SimpMessageDTO implements Serializable {
    private static final long serialVersionUID = 3498566916053367063L;

    /**
     * ID
     */
    private Long id;

    /**
     * 消息内容
     */
    private String messageContent;

    /**
     * 消息类型
     */
    private Integer messageType;

    /**
     * 消息类型
     */
    private String messageTypeStr;

    /**
     * 跳转用参数 json格式
     */
    private String jumpParameters;

    /**
     * 消息状态 0-未读，1-已读
     */
    private Integer messageStatus;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;
}
