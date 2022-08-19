package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 采集项的次分类
 *
 * @author zhaoff
 */
@Slf4j
@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherXmlCategoryBean implements Serializable, Cloneable {

    private static final long serialVersionUID = -4600769792140927912L;

    /**
     * 次分类的名字
     */
    @XmlAttribute
    private String name;

    /**
     * 次分类显示的名称
     */
    @XmlAttribute
    private String display;

    /**
     * 此次分类下的所有采集项分组
     */
    @XmlElement(name = "group")
    private List<GatherGroup> group;

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 返回该次分类下所有采集指标项
     *
     * @return 所有采集指标项
     */
    public List<GatherDetail> listIndicators() {
        List<GatherDetail> list = new ArrayList<>();
        for (GatherGroup group : getGroup()) {
            list.addAll(group.listIndicators());
            if (null != group.getGroup()) {
                for (GatherGroup chdGro : group.getGroup()) {
                    chdGro.setParentGroup(group);
                    listIndicators(list, chdGro);
                }
            }
        }
        return list;
    }

    /**
     * 递归获取分组中的所有采集指标项
     *
     * @param list       存放采集指标项的列表
     * @param childGroup 采集分组信息
     */
    public void listIndicators(List<GatherDetail> list, GatherGroup childGroup) {
        list.addAll(childGroup.listIndicators());
        if (null != childGroup.getGroup()) {
            for (GatherGroup chdGro : childGroup.getGroup()) {
                chdGro.setParentGroup(childGroup);
                listIndicators(list, chdGro);
            }
        }
    }

    /**
     * 将另一个次分类合并到此次分类中
     *
     * @param newCategory 待合并的采集项的次分类
     */
    public void merge(GatherXmlCategoryBean newCategory) {
        List<GatherGroup> originGroups = this.getGroup();
        List<GatherGroup> newGroups = newCategory.getGroup();
        if (null == originGroups) {
            this.setGroup(newGroups);
            return;
        }
        if (null == newGroups) {
            return;
        }
        for (GatherGroup newGroup : newGroups) {
            boolean found = false;
            for (GatherGroup originGroup : originGroups) {
                if (newGroup.getName().equals(originGroup.getName())) {
                    originGroup.merge(newGroup);
                    found = true;
                    break;
                }
            }
            if (!found) {
                originGroups.add(newGroup);
            }
        }
    }

    @Override
    public GatherXmlCategoryBean clone() throws CloneNotSupportedException {
        GatherXmlCategoryBean bean = null;
        List<GatherGroup> copyGroup;
        try {
            bean = (GatherXmlCategoryBean) super.clone();
            JavaType javaTypeGro = mapper.getTypeFactory().constructParametricType(List.class, GatherGroup.class);
            copyGroup = mapper.readValue(mapper.writeValueAsBytes(this.group), javaTypeGro);
            bean.setGroup(copyGroup);
        } catch (IOException e) {
            if (log.isWarnEnabled()) {
                log.warn("Clone GatherXmlCategoryBean error: ", e);
            }
        }
        return bean;
    }

}
