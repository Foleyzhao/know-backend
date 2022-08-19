package com.cumulus.modules.mnt.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 运维管理：数据库实体
 */
@Entity
@Getter
@Setter
@Table(name = "mnt_database")
public class Database extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2256048875710994059L;

    /**
     * ID
     */
    @Id
    private String id;

    /**
     * 数据库名称
     */
    private String name;

    /**
     * 数据库连接地址
     */
    private String jdbcUrl;

    /**
     * 数据库密码
     */
    private String pwd;

    /**
     * 用户名
     */
    private String userName;

    public void copy(Database source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
