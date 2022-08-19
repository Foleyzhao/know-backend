package com.cumulus.modules.business.gather.entity.es;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 中间件实体
 *
 * @author shijh
 */
@Getter
@Setter
@Document(indexName = "middleware")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MiddlewareEs implements Serializable {

    private static final long serialVersionUID = 1693640877196692408L;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

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
     * 版本
     */
    @Field(type = FieldType.Keyword)
    private String version;

    /**
     * 端口
     */
    @Field(type = FieldType.Integer)
    private Integer port;

    /**
     * 安装路径
     */
    @Field(type = FieldType.Keyword)
    private String installationPath;
    ;

    /**
     * 应用路径
     */
    @Field(type = FieldType.Keyword)
    private String appPath;

    /**
     * 启动用户
     */
    @Field(type = FieldType.Keyword)
    private String startUser;

    /**
     * 开发语音
     */
    @Field(type = FieldType.Keyword)
    private String devLanguage;

    /**
     * 应用插件
     */
    @Field(type = FieldType.Keyword)
    private String appPlugin;

    /**
     * web地址
     */
    @Field(type = FieldType.Keyword)
    private String webAddress;

    /**
     * 类型
     */
    @Field(type = FieldType.Keyword)
    private String type;

    /**
     * 收集时间
     */
    @Field(type = FieldType.Date)
    private Date utime;

}
