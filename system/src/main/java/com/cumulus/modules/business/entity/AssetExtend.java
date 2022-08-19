package com.cumulus.modules.business.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 资产属性扩展表
 *
 * @author zhangxq
 */
@Setter
@Getter
@Entity
@Table(name = "tbl_asset_extend")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@DynamicUpdate
public class AssetExtend implements Serializable {

    private static final long serialVersionUID = 6071768644853350252L;

    public static final String TYPE_TCP = "tcp";
    public static final String TYPE_UDP = "udp";


    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 主机uuid
     */
    private String uuid;

    /**
     * 位置
     */
    private String location;

    /**
     * 上次探测时间
     */
    @Column(name = "last_detect_time")
    private Timestamp lastDetectTime;

    /**
     * 服务
     */
    private String server;

    /**
     * 服务组件
     */
    private String serverComponent;

    /**
     * 标题
     */
    private String title;

    /**
     * 网址
     */
    private String website;

    /**
     * 引擎返回json
     */
    private String json;

    /**
     * 以下为引擎返回信息
     * 类型 tcp/udp
     */
    private String type;

    /**
     * 状态：'open'/'closed'/'filtered'
     */
    private String state;

    /**
     * 状态判断依据
     */
    private String reason;

    /**
     * 可信度
     */
    private Integer conf;

    /**
     * 服务名称
     */
    private String name;

    /**
     * 服务产品名称
     */
    private String product;

    /**
     * 版本号
     */
    private String version;

    /**
     * 端口banner信息
     */
    private String banner;

    /**
     * 额外信息
     */
    private String extrainfo;

    /**
     * CPE信息
     */
    private String cpe;

    /**
     * 纬度
     */
    private String lat;

    /**
     * 经度
     */
    private String lon;

}
