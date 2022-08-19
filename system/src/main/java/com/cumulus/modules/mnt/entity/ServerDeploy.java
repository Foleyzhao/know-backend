package com.cumulus.modules.mnt.entity;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.cumulus.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * 运维管理：服务器实体
 */
@Entity
@Getter
@Setter
@Table(name = "mnt_server")
public class ServerDeploy extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5794387337620029814L;
    
    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 服务器名称
     */
    private String name;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    public void copy(ServerDeploy source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        ServerDeploy that = (ServerDeploy) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

}
