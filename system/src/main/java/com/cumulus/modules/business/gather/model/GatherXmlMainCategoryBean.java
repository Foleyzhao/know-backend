package com.cumulus.modules.business.gather.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 采集项主类
 *
 * @author zhaoff
 */
@Getter
@Setter
@XmlRootElement(name = "maincategory")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GatherXmlMainCategoryBean implements Cloneable, Serializable {

    private static final long serialVersionUID = -5201574845118399372L;

    /**
     * 主分类的名字
     */
    @XmlAttribute
    private String name;

    /**
     * 优先级
     */
    @XmlAttribute
    private Integer priority = Integer.MAX_VALUE;

    /**
     * 主分类下的所有次分类
     */
    private List<GatherXmlCategoryBean> category;

    /**
     * 主分类显示的名称
     */
    @XmlAttribute
    private String display;

    /**
     * 类与对象映射对象
     */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 将另一个采集项主类合并到此主分类中
     *
     * @param newMainCategory 待合并的采集项主类
     */
    public void merge(GatherXmlMainCategoryBean newMainCategory) {
        List<GatherXmlCategoryBean> originCategoryList = this.getCategory();
        List<GatherXmlCategoryBean> newCategoryList = newMainCategory.getCategory();
        if (null == originCategoryList) {
            this.setCategory(newCategoryList);
            return;
        }
        if (null == newCategoryList) {
            return;
        }
        for (GatherXmlCategoryBean newCategory : newCategoryList) {
            boolean found = false;
            for (GatherXmlCategoryBean originCategory : originCategoryList) {
                if (newCategory.getName().equals(originCategory.getName())) {
                    originCategory.merge(newCategory);
                    found = true;
                    break;
                }
            }
            if (!found) {
                originCategoryList.add(newCategory);
            }
        }
    }

    /**
     * 获取采集项的次分类
     *
     * @param category 采集项的次分类的名称
     * @return 采集项的次分类
     */
    public GatherXmlCategoryBean queryCategory(String category) {
        if (null == this.getCategory()) {
            return null;
        }
        for (GatherXmlCategoryBean s : this.getCategory()) {
            if (s.getName().equals(category)) {
                return s;
            }
        }
        return null;
    }

    /**
     * 获取采集项的次分类列表
     *
     * @param category 分类的名称
     * @return 采集项次分类列表
     */
    public List<GatherXmlCategoryBean> queryCategoryList(String category) {
        List<GatherXmlCategoryBean> categoryList = new ArrayList<>();
        if (null == this.getCategory()) {
            return null;
        }
        for (GatherXmlCategoryBean s : this.getCategory()) {
            if (s.getName().equals(category)) {
                categoryList.add(s);
            }
        }
        return categoryList;
    }

    /**
     * 获取所有采集指标项
     *
     * @return 所有采集指标项
     */
    public List<GatherDetail> listIndicators() {
        List<GatherDetail> list = new ArrayList<>();
        if (null == this.getCategory()) {
            return list;
        }
        for (GatherXmlCategoryBean s : this.getCategory()) {
            list.addAll(s.listIndicators());
        }
        return list;
    }

    @Override
    public GatherXmlMainCategoryBean clone() {
        GatherXmlMainCategoryBean bean;
        List<GatherXmlCategoryBean> copyCategory;
        try {
            bean = (GatherXmlMainCategoryBean) super.clone();
            JavaType javaTypeGro = mapper.getTypeFactory().constructParametricType(List.class,
                   GatherXmlCategoryBean.class);
            copyCategory = mapper.readValue(mapper.writeValueAsBytes(this.category), javaTypeGro);
            bean.setCategory(copyCategory);
        } catch (Exception e) {
            throw new InternalError(e);
        }
        return bean;
    }

}
