package com.cumulus.modules.system.entity;

import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * 系统消息表
 *
 * @author : shenjc
 */
@Getter
@Setter
@Entity
@Table(name = "sys_message")
public class Message extends BaseEntity {
    private static final long serialVersionUID = 5668510231006538287L;

    /**
     * 消息状态 0-未读，1-已读
     */
    public static final int MESSAGE_STATUS_UNREAD = 0;
    public static final int MESSAGE_STATUS_READING = 1;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
     * 跳转用参数 json格式
     */
    private String jumpParameters;

    /**
     * 用户Id
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 消息状态 0-未读，1-已读
     */
    private Integer messageStatus;
}
