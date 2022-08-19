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
 * 硬件信息
 *
 * @author shijh
 */
@Data
@Document(indexName = "hardware")
public class HardwareEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = 7992174553875032911L;

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
     * 磁盘大小 GB
     */
    @Field(type = FieldType.Double)
    private Double diskSize;

    /**
     * cpu 型号
     */
    @Field(type = FieldType.Keyword)
    private String cpuModel;

    /**
     * cpu 核数
     */
    @Field(type = FieldType.Keyword)
    private String cpuCores;

    /**
     * 硬件型号
     */
    @Field(type = FieldType.Keyword)
    private String systemModel;

    /**
     * cpu 主频
     */
    @Field(type = FieldType.Keyword)
    private String cpuFrequency;

    /**
     * 电源功率
     */
    @Field(type = FieldType.Keyword)
    private String power;

    /**
     * 电源数量
     */
    @Field(type = FieldType.Keyword)
    private String powerNum;

    /**
     * 内存大小
     */
    @Field(type = FieldType.Keyword)
    private String memorySize;

    /**
     * 内存数量
     */
    @Field(type = FieldType.Keyword)
    private String memoryNum;

    /**
     * 启动时间 (启动了多久)
     */
    @Field(type = FieldType.Keyword)
    private String startTime;

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
