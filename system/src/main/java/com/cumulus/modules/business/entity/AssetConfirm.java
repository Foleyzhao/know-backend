package com.cumulus.modules.business.entity;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import com.cumulus.base.BaseEntity;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.system.entity.Dept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 确认资产实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_asset_confirm")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AssetConfirm extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 7768889293642486914L;

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ip
     */
    private String ip;

    /**
     * ip全写
     */
    private String completeIp;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 开放端口
     */
    private String openPort;

    /**
     * 资产类别 主机 应用
     */
    private Integer assetCategory;

    /**
     * 资产类型
     */
    @ManyToOne
    @JoinColumn(name = "asset_type_id")
    private AssetType assetType;

    /**
     * 资产系统类型
     */
    @ManyToOne
    @JoinColumn(name = "asset_sys_type_id")
    private AssetSysType assetSysType;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 服务
     */
    private String server;

    /**
     * 网址
     */
    private String website;

    /**
     * 采集方式 1-Agent 2-登录采集 3-无访问
     */
    private Integer gatherType;

    /**
     * 发现任务
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "detect_task_id", referencedColumnName = "id")
    private DetectTask detectTask;

    /**
     * 任务名称
     */
    @Column(name = "detect_task_name")
    private String detectTaskName;

    /**
     * 资产扩展属性
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "extend_id")
    private AssetExtend assetExtend;

    /**
     * 所属部门
     */
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Dept dept;

    /**
     * 是否在线
     */
    private Boolean online = true;

    /**
     * 复制属性
     *
     * @param assetConfirm 确认资产
     * @return 新确认资产
     */
    public static AssetConfirm copy(AssetConfirm assetConfirm) {
        AssetConfirm newAsset = new AssetConfirm();
        newAsset.setIp(assetConfirm.getIp());
        newAsset.setCompleteIp(assetConfirm.getCompleteIp());
        newAsset.setDetectTask(assetConfirm.getDetectTask());
        newAsset.setDetectTaskName(assetConfirm.getDetectTaskName());
        newAsset.setAssetCategory(assetConfirm.getAssetCategory());
        newAsset.setDept(assetConfirm.getDept());
        return newAsset;
    }

}
