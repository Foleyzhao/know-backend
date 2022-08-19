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
 * 账号信息
 *
 * @author shijh
 */
@Data
@Document(indexName = "account")
public class AccountEs implements Serializable ,GatherInstance {

    private static final long serialVersionUID = 5205009590232290031L;

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
     * 账号信息
     */
    @Field(type = FieldType.Keyword)
    private String accountName;

    /**
     * 账号到期时间
     */
    @Field(type = FieldType.Keyword)
    private String accountExpireData;

    /**
     * 密码到期时间
     */
    @Field(type = FieldType.Keyword)
    private String pwdExpireData;

    /**
     * 所属组
     */
    @Field(type = FieldType.Keyword)
    private String group;

    /**
     * 上次设置密码时间
     */
    @Field(type = FieldType.Date)
    private Date pwdLastSetTime;

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
