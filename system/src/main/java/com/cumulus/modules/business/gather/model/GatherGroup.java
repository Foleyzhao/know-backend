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
import java.util.ArrayList;
import java.util.List;

/**
 * 采集项分组
 *
 * @author zhaoff
 */
@Slf4j
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherGroup implements Cloneable, Serializable {

    private static final long serialVersionUID = -3501662536128618378L;

    /**
     * 分组的名字
     */
    @XmlAttribute
    private String name;

    /**
     * 分组显示的名称
     */
    @XmlAttribute
    private String display;

    /**
     * 该分组下的所有的采集项
     */
    private List<GatherDetail> indicator;

    /**
     * 子分组
     */
    private List<GatherGroup> group;

    /**
     * 分组类型：实时，耗时，不常变化
     */
    @XmlAttribute
    private Integer type;

    /**
     * 所在的组
     */
    @XmlTransient
    @JsonIgnore
    private GatherGroup parentGroup;

    /**
     * 优先级
     */
    @XmlAttribute
    private Integer priority = Integer.MAX_VALUE;

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 获取采集项key
     *
     * @return 采集项key
     */
    public String itemKey() {
        return parentGroup == null ? name : parentGroup.itemKey() + "." + name;
    }

    /**
     * 返回该分组下所有采集指标项
     *
     * @return 所有采集指标项
     */
    public List<GatherDetail> listIndicators() {
        List<GatherDetail> list = new ArrayList<>();
        listAllIndicators(this, list);
        return list;
    }

    /**
     * 递归获取某个分组下的所有采集指标项
     *
     * @param parent 父分组
     * @param list   所有采集指标项
     */
    private void listAllIndicators(GatherGroup parent, List<GatherDetail> list) {
        if (null != parent.getIndicator()) {
            parent.getIndicator().forEach(e -> e.setParentGroup(parent));
            list.addAll(parent.getIndicator());
        }
        if (null == parent.getGroup()) {
            return;
        }
        for (GatherGroup group : parent.getGroup()) {
            listAllIndicators(group, list);
        }
    }

    /**
     * 获取祖先分组（父分组的父分组）
     *
     * @return 祖先分组
     */
    @XmlTransient
    @JsonIgnore
    public GatherGroup getAncestorGroup() {
        return null == parentGroup ? this : parentGroup.getParentGroup();
    }

    /**
     * 将新的采集分组合并到此采集分组中
     *
     * @param newGroup 待合并的采集分组
     */
    public void merge(GatherGroup newGroup) {
        mergeGatherGroup(this, newGroup);
    }

    /**
     * 递归将两个采集分组合并
     *
     * @param originGroup 源采集分组
     * @param newGroup    待合并采集分组
     */
    private void mergeGatherGroup(GatherGroup originGroup, GatherGroup newGroup) {
        List<GatherGroup> originSubGroups = originGroup.getGroup();
        List<GatherGroup> newSubGroups = newGroup.getGroup();
        if (null != newSubGroups) {
            if (null == originSubGroups) {
                originGroup.setGroup(newSubGroups);
            } else {
                for (GatherGroup newSubGroup : newSubGroups) {
                    boolean found = false;
                    for (GatherGroup originSubGroup : originSubGroups) {
                        if (newSubGroup.getName().equals(originSubGroup.getName())) {
                            mergeGatherGroup(originSubGroup, newSubGroup);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        originSubGroups.add(newSubGroup);
                    }
                }
            }
        }
        if (null != originSubGroups) {
            originSubGroups.forEach(e -> {
                e.setParentGroup(originGroup);
            });
        }

        if (null == originGroup.getIndicator()) {
            originGroup.setIndicator(newGroup.getIndicator());
            return;
        }

        if (null == newGroup.getIndicator()) {
            return;
        }

        for (GatherDetail detail : newGroup.getIndicator()) {
            boolean found = false;
            for (GatherDetail originDetail : originGroup.getIndicator()) {
                if (detail.getName().equals(originDetail.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                originGroup.getIndicator().add(detail);
            }
        }
    }

    @Override
    public GatherGroup clone() throws CloneNotSupportedException {
        GatherGroup gatherGroup = null;
        List<GatherGroup> copyGroups;
        List<GatherDetail> copyGatherDetail;
        GatherGroup copyParentGroup;
        try {
            gatherGroup = (GatherGroup) super.clone();
            copyParentGroup = mapper.readValue(mapper.writeValueAsBytes(this.parentGroup), GatherGroup.class);
            gatherGroup.setParentGroup(copyParentGroup);
            JavaType javaTypeGro = mapper.getTypeFactory().constructParametricType(List.class, GatherGroup.class);
            copyGroups = mapper.readValue(mapper.writeValueAsBytes(this.group), javaTypeGro);
            if (null != copyGroups && copyGroups.size() > 0) {
                for (GatherGroup bg : copyGroups) {
                    bg.setParentGroup(gatherGroup);
                }
            }
            gatherGroup.setGroup(copyGroups);
            JavaType javaTypeGatherDetail =
                    mapper.getTypeFactory().constructParametricType(List.class, GatherDetail.class);
            copyGatherDetail = mapper.readValue(mapper.writeValueAsBytes(this.indicator), javaTypeGatherDetail);
            if (null != copyGatherDetail && copyGatherDetail.size() > 0) {
                for (GatherDetail bd : copyGatherDetail) {
                    bd.setParentGroup(gatherGroup);
                }
            }
            gatherGroup.setIndicator(copyGatherDetail);

        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Clone GatherGroup error: ", e);
            }
        }
        return gatherGroup;
    }
}
