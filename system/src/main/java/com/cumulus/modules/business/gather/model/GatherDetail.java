package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * 采集项的详情
 *
 * @author zhaoff
 */
@Slf4j
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherDetail implements Cloneable, Serializable {

    private static final long serialVersionUID = 2988046202911457340L;

    /**
     * 采集项的名字
     */
    @XmlAttribute
    private String name;

    /**
     * 采集项显示的名字
     */
    @XmlAttribute
    private String display;

    /**
     * 预采集的名字
     */
    @XmlAttribute
    private String require;

    /**
     * 成立的条件
     */
    @XmlAttribute(name = "if")
    private String ifParam;

    /**
     * 采集指令
     */
    private GatherDetailCommands commands;

    /**
     * 输出项
     */
    private List<GatherDetailOutput> output;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 优先级
     */
    @XmlAttribute
    Integer priority = Integer.MAX_VALUE;

    /**
     * 所在的组
     */
    @XmlTransient
    @JsonIgnore
    private GatherGroup parentGroup;

    /**
     * 采集类型（0：实时项，1：耗时项，2：不常变化项）
     */
    @XmlAttribute
    private Integer collectType;

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 获取完整的itemKey
     *
     * @return 完整的itemKey
     */
    public String itemKey() {
        return parentGroup.itemKey() + "." + this.getName();
    }

    /**
     * 获取父分组显示的名字
     *
     * @return 父分组显示的名字
     */
    public String groupDisplayName() {
        return parentGroup.getDisplay();
    }

    /**
     * 获取祖先分组（父分组的父分组）
     *
     * @return 祖先分组
     */
    @XmlTransient
    @JsonIgnore
    public GatherGroup getAncestorGroup() {
        return null == parentGroup ? null : parentGroup.getParentGroup();
    }

    @Override
    public GatherDetail clone() throws CloneNotSupportedException {
        GatherDetail ret = (GatherDetail) super.clone();
        if (null != commands) {
            ret.commands = commands.clone();
        }
        if (null != output) {
            ret.output = new LinkedList<>();
            for (GatherDetailOutput item : output) {
                ret.output.add(item.clone());
            }
        }
        //实体克隆
        List<GatherDetailOutput> copyGatherDetailOutput;
        GatherGroup parentGroup;
        try {
            parentGroup = mapper.readValue(mapper.writeValueAsBytes(this.parentGroup), GatherGroup.class);
            ret.setParentGroup(parentGroup);
            JavaType javaTypeGatherDetailOutput =
                    mapper.getTypeFactory().constructParametricType(List.class, GatherDetailOutput.class);
            copyGatherDetailOutput = mapper.readValue(mapper.writeValueAsBytes(this.commands),
                    javaTypeGatherDetailOutput);
            ret.setOutput(copyGatherDetailOutput);

        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Clone GatherDetail error: ", e);
            }
        }
        return ret;
    }

}
