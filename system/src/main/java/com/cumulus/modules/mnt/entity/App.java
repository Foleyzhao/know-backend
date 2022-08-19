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

/**
 * 运维管理：应用实体
 */
@Entity
@Getter
@Setter
@Table(name = "mnt_app")
public class App extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1784770110106660586L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 端口
     */
    private int port;

    /**
     * 上传路径
     */
    private String uploadPath;

    /**
     * 部署路径
     */
    private String deployPath;

    /**
     * 备份路径
     */
    private String backupPath;

    /**
     * 启动脚本
     */
    private String startScript;

    /**
     * 部署脚本
     */
    private String deployScript;

    public void copy(App source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

}
