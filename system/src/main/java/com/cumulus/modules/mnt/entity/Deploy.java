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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Set;

/**
 * 运维管理：部署详情实体
 */
@Entity
@Getter
@Setter
@Table(name = "mnt_deploy")
public class Deploy extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -7311475716444544278L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 服务器
     */
    @ManyToMany
    @JoinTable(name = "mnt_deploy_server",
            joinColumns = {@JoinColumn(name = "deploy_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "server_id", referencedColumnName = "id")})
    private Set<ServerDeploy> deploys;

    /**
     * 应用
     */
    @ManyToOne
    @JoinColumn(name = "app_id")
    private App app;

    public void copy(Deploy source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
