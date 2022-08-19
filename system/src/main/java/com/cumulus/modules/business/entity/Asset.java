package com.cumulus.modules.business.entity;

import com.cumulus.base.BaseEntity;
import com.cumulus.modules.system.entity.Dept;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 资产实体类
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_asset")
@DynamicUpdate
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Asset extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 2466230002493909530L;

    /**
     * 1主机资产 2应用资产
     */
    public static final int CATEGORY_HOST = 1;
    public static final int CATEGORY_PORT = 2;

    /**
     * 风险级别安全 0安全 1低危 2中危 3高危险
     */
    public static final int RISK_SAFETY = 0;
    public static final int RISK_LOW = 1;
    public static final int RISK_MIDDLE = 2;
    public static final int RISK_HIGH = 3;

    /**
     * 风险级别安全名称 0安全 1低危 2中危 3高危险
     */
    public static final String RISK_SAFETY_NAME = "安全资产";
    public static final String RISK_LOW_NAME = "低危资产";
    public static final String RISK_MIDDLE_NAME = "中危资产";
    public static final String RISK_HIGH_NAME = "高危资产";

    /**
     * 风险级别安全 0存活 1下线 2异常
     */
    public static final int STATUS_SURVIVE = 0;
    public static final int STATUS_OFFLINE = 1;
    public static final int STATUS_ABNORMAL = 2;


    /**
     * ip 和端口的分隔符
     */
    public static final String IP_PORT_SEPARATE_STR = ":";


    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP
     */
    private String ip;

    /**
     * 资产名称
     */
    private String name;

    /**
     * IP全写
     */
    private String completeIp;

    /**
     * 端口
     */
    private Integer port;

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
     * 资产类别（主机，应用）
     */
    private Integer assetCategory;

    /**
     * 所属部门
     */
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Dept dept;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 资产编码类型（ISO-8859-1/GB18030/US-ASCII/UTF-8）
     */
    @Column(name = "charset")
    private String charset;

    /**
     * 资产扩展属性
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "extend_id")
    private AssetExtend assetExtend;

    /**
     * 资产状态
     */
    private Integer assetStatus;

    /**
     * 风险等级 0 安全 1 低危 2 中危 3 高危
     */
    private Integer riskLevel;

    /**
     * 在线时长
     */
    private Integer onlineTime;

    /**
     * agent状态
     */
    private Integer agentStatus;

    /**
     * 资产标签
     */
    @ManyToMany
    @JoinTable(name = "tbl_asset_tag_mapping",
            joinColumns = {@JoinColumn(name = "asset_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
    private Set<AssetTag> assetTags = new HashSet<>();

    /**
     * 父资产
     */
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Asset parent;

    /**
     * web地址
     */
    private String webAddress;

    /**
     * 指纹信息
     */
    private String fingerprint;

    /**
     * ES的扫描资产id 空代表没有扫描过
     */
    private String gatherAssetId;

    /**
     * ES的远程扫描资产id 空代表没有扫描过
     */
    private String scanAssetId;

    /**
     * 登录测试状态 0 未开始 1 执行中 2 成功 3 失败
     */
    private Integer loginStatus;

    /**
     * 采集方式 1-Agent 2-登录采集 3-远程扫描
     * <p>
     * 远程扫描，远程采集，agent采集，默认为远程扫描
     */
    private Integer gatherType;

    /**
     * 是否在线
     */
    private Boolean online = true;

    /**
     * 采集配置
     */
    private String config;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        Asset asset = (Asset) o;
        return Objects.equals(id, asset.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
