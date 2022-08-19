package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 端口
 *
 * @author Shijh
 */
@Data
@Document(indexName = "port")
public class PortEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = -4772405195975855771L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 采集资产ID
     */
    @Field(type = FieldType.Keyword)
    private String gatherAssetId;

    /**
     * 资产ID
     */
    @Field(type = FieldType.Long)
    private Long assetId;

    /**
     * 端口号
     */
    @Field(type = FieldType.Integer)
    private Integer name;

    /**
     * 服务状态
     */
    @Field(type = FieldType.Keyword)
    private String state;

    /**
     * 协议类型
     */
    @Field(type = FieldType.Keyword)
    private String protocol;

    /**
     * 进程ID
     */
    @Field(type = FieldType.Integer)
    private Integer processId;

    /**
     * 程序名
     */
    @Field(type = FieldType.Keyword)
    private String program;

    /**
     * 本地地址
     */
    @Field(type = FieldType.Keyword)
    private String localAddress;

    /**
     * 本地端口
     */
    @Field(type = FieldType.Keyword)
    private String localPort;

    /**
     * 外部地址
     */
    @Field(type = FieldType.Keyword)
    private String externalAddress;

    /**
     * 外部端口
     */
    @Field(type = FieldType.Keyword)
    private String externalPort;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

    /**
     * 风险类型
     */
    @Field(type = FieldType.Object)
    private List<Long> riskType;

    /**
     * 风险等级
     */
    @Field(type = FieldType.Integer)
    private Integer riskLevel;

    /**
     * 详情
     */
    @Field(type = FieldType.Object)
    private Map<String, Object> detail;

}
