package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 采集输出项
 *
 * @author zhaoff
 */
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherDetailOutput implements Cloneable, Serializable {

    private static final long serialVersionUID = -2350333158465999412L;

    /**
     * 成立的条件
     */
    @XmlAttribute(name = "if")
    private String ifParam;

    /**
     * 所有的采集结果生成的变量
     */
    private List<GatherDetailVariable> variable;

    @Override
    public GatherDetailOutput clone() throws CloneNotSupportedException {
        GatherDetailOutput ret = (GatherDetailOutput) super.clone();
        if (null != variable) {
            ret.variable = new LinkedList<>();
            for (GatherDetailVariable v : variable) {
                ret.variable.add(v.clone());
            }
        }
        return ret;
    }
}
