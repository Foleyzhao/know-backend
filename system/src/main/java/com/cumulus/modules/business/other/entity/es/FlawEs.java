package com.cumulus.modules.business.other.entity.es;


import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 漏洞表
 *
 * @author Shijh
 */
@Data
@Document(indexName = "flaw")
public class FlawEs implements Serializable {

    private static final long serialVersionUID = -6623213133108727950L;

    public static final int HANDLE_STATUS_NEW = 0;

    /**
     * ID
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String id;

    /**
     * 资产IP
     */
    @Field(type = FieldType.Keyword)
    private String ip;

    /**
     * 风险名称
     */
    @Field(type = FieldType.Keyword)
    private String name;

    /**
     * 风险归属
     */
    @Field(type = FieldType.Keyword)
    private String dept;

    /**
     * 风险编号
     */
    @Field(type = FieldType.Keyword)
    private String number;

    /**
     * 漏洞来源
     */
    @Field(type = FieldType.Keyword)
    private String source;

    /**
     * 处理状态（0：新发现 1:待审核 2：待下发 3：待修复 4：待复测 5：待关闭6：已修复 7：已忽略）
     */
    @Field(type = FieldType.Integer)
    private Integer handleStatus;

    /**
     * 漏洞等级 (0:低危 1:中危 2高危)
     */
    @Field(type = FieldType.Integer)
    private Integer grade;

    /**
     * 漏洞描述
     */
    @Field(type = FieldType.Keyword)
    private String description;

    /**
     * 漏洞细节
     */
    @Field(type = FieldType.Keyword)
    private String details;

    /**
     * 漏洞影响
     */
    @Field(type = FieldType.Keyword)
    private String affect;

    /**
     * 解决方案
     */
    @Field(type = FieldType.Keyword)
    private String solution;

    /**
     * 漏洞cvss分值(主机漏洞)
     */
    @Field(type = FieldType.Keyword)
    private String score;

    /**
     * 漏洞分类（web漏洞）
     */
    @Field(type = FieldType.Keyword)
    private String classify;

    /**
     * 漏洞标识（CVE/CNVD）
     */
    @Field(type = FieldType.Keyword)
    private String identifying;

    /**
     * 相关端口
     */
    @Field(type = FieldType.Integer)
    private Integer port;

    /**
     * 发现时间
     */
    @Field(type = FieldType.Date)
    private Date utime;


}
