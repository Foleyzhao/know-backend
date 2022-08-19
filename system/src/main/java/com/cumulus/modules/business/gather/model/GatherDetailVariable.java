package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

/**
 * 采集结果生成的变量
 *
 * @author zhaoff
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherDetailVariable implements Cloneable, Serializable {

    private static final long serialVersionUID = -2737823718461784760L;

    /**
     * 变量的名字
     */
    @XmlElement(name = "name")
    private String name;

    /**
     * 变量的值类型
     */
    @XmlElement(name = "type")
    private String valueType;

    /**
     * 变量的描述信息
     */
    @XmlElement(name = "description")
    private String valueDescription;

    /**
     * el表达式
     */
    @XmlElement(name = "eval-el")
    private String evalEl;

    /**
     * groovy表达式
     */
    @XmlElement(name = "script")
    private String script;

    /**
     * 操作（例如："=",">"）
     */
    @XmlElement(name = "operator")
    private String operator;

    /**
     * 采集结果的可选值
     */
    @XmlElement(name = "data")
    private List<String> data;

    @Override
    public GatherDetailVariable clone() {
        try {
            return (GatherDetailVariable) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public String toString() {
        return "GatherDetailVariable [name=" + name + ", valueType=" + valueType + ", valueDescription="
                + valueDescription + ", evalEl=" + evalEl + ", script=" + script + ", operator=" + operator
                + ", data=" + data + "]";
    }
}
