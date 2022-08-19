package com.cumulus.modules.mnt.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 运维管理：部署历史记录实体
 */
@Entity
@Getter
@Setter
@Table(name = "mnt_deploy_history")
public class DeployHistory implements Serializable {

    private static final long serialVersionUID = 3068631294709805829L;

    /**
     * ID
     */
    @Id
    private String id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 服务器IP
     */
    private String ip;

    /**
     * 部署时间
     */
    @CreationTimestamp
    private Timestamp deployDate;

    /**
     * 部署者
     */
    private String deployUser;

    /**
     * 部署ID
     */
    private Long deployId;

    public void copy(DeployHistory source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
