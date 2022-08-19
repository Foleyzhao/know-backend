package com.cumulus.modules.system.dto;

import com.cumulus.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 系统信息查询对象
 *
 * @author : shenjc
 */
@Data
public class MessageQueryCriteria {

    /**
     * 外键 用户id
     */
    @Query(propName = "id", joinName = "user")
    private Long userId;

    /**
     * 消息状态 0-未读，1-已读
     */
    @Query
    private Integer messageStatus;

    /**
     * 创建时间
     */
    @Query(propName = "createTime", type = Query.Type.GREATER_THAN)
    private Timestamp createTimeGreaterThan;
}
