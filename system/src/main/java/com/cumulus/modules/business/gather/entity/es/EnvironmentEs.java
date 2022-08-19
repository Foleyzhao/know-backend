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
 * 环境变量
 *
 * @author Shijh
 */
@Data
@Document(indexName = "environment")
public class EnvironmentEs implements Serializable, GatherInstance {

    private static final long serialVersionUID = -3812036236120613110L;

    /**
     * ID
     */
    @Id
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
     * 变量类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 变量名
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 变量值
     */
    @Field(type = FieldType.Keyword)
    private String value;

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

    @Override
    public void saveChangeContent() {
        // 数组[name detail]
    }

    @Override
    public void isSaveChange() {
        return;// true
    }
}
