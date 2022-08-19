package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

/**
 * 采集项配置命令实体
 *
 * @author zhaoff
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherDetailCommand implements Cloneable, Serializable {

    private static final long serialVersionUID = -5351779993878866338L;

    /**
     * 逻辑分支判断表达式
     */
    @XmlAttribute(name = "if")
    private String ifParam;

    /**
     * 输出变量名称
     */
    @XmlAttribute
    private String output;

    /**
     * 命令执行等待时间
     */
    @XmlAttribute(name = "wait")
    private Integer lot;

    /**
     * 命令
     */
    @XmlValue
    private String command;

    @Override
    public GatherDetailCommand clone() throws CloneNotSupportedException {
        return (GatherDetailCommand) super.clone();
    }

}
