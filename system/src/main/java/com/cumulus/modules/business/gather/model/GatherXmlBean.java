package com.cumulus.modules.business.gather.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于映射采集配置文件的Bean
 *
 * @author zhaoff
 */
@Getter
@Setter
public class GatherXmlBean {

    /**
     * 所有的主分类
     */
    private List<GatherXmlMainCategoryBean> mainCategory = new ArrayList<>();

    /**
     * itemKey和主分类映射关系
     */
    private Map<String, GatherXmlMainCategoryBean> indicatorMainMap = new HashMap<>();

    /**
     * itemKey和采集项的详情映射关系
     */
    private Map<String, GatherDetail> indicatorMap = new HashMap<>();

    /**
     * 将指定主分类添加到主分类列表中
     *
     * @param newMainCategory 指定的主分类
     */
    public void merge(GatherXmlMainCategoryBean newMainCategory) {
        boolean found = false;
        for (GatherXmlMainCategoryBean item : mainCategory) {
            if (item.getName().equals(newMainCategory.getName())) {
                item.merge(newMainCategory);
                found = true;
                break;
            }
        }
        if (!found) {
            mainCategory.add(newMainCategory);
        }
    }

    /**
     * 主分类根据优先级进行排序
     */
    private void sort() {
        this.mainCategory.sort(Comparator.comparing(GatherXmlMainCategoryBean::getPriority));
    }

    /**
     * 读取xml文件成功后排序merge
     */
    public void buildComplete() {
        clear();
        sort();
        mainCategory.forEach(main -> main.listIndicators().forEach(indicator -> buildMap(main, indicator)));
    }

    /**
     * 清空所有内存变量
     */
    private void clear() {
        indicatorMainMap.clear();
        indicatorMap.clear();
    }

    /**
     * 构造所有内存变量
     *
     * @param main      主分类
     * @param indicator 采集指标项
     */
    private void buildMap(GatherXmlMainCategoryBean main, GatherDetail indicator) {
        indicatorMainMap.putIfAbsent(indicator.itemKey(), main);
        indicatorMap.putIfAbsent(indicator.itemKey(), indicator);
    }

    /**
     * 根据itemKey获取采集项主类
     *
     * @param itemKey itemKey
     * @return 采集项详情
     */
    public GatherXmlMainCategoryBean queryMainCategoryByItemKey(String itemKey) {
        return indicatorMainMap.get(itemKey);
    }

    /**
     * 根据显示名称获取采集项主类列表
     *
     * @param display 显示名称
     * @return 采集项主类列表
     */
    public List<GatherXmlMainCategoryBean> queryMainCategoryByDisplay(String display) {
        List<GatherXmlMainCategoryBean> lists = new ArrayList<>();
        for (GatherXmlMainCategoryBean main : indicatorMainMap.values()) {
            if (main.getDisplay().endsWith(display)) {
                lists.add(main);
            }
        }
        return lists;
    }

    /**
     * 获取采集项详情
     *
     * @param itemKey itemKey
     * @return 采集项详情
     */
    public GatherDetail queryBaselineDetail(String itemKey) {
        return indicatorMap.get(itemKey);
    }


    /**
     * 获取采集项主类
     *
     * @param mainCategory 主类
     * @return 采集项主类
     */
    public GatherXmlMainCategoryBean queryMainCategory(String mainCategory) {
        for (GatherXmlMainCategoryBean m : this.mainCategory) {
            if (m.getName().equals(mainCategory)) {
                return m;
            } else if (m.getName().contains(",")) {
                String[] names = m.getName().split(",");
                for (String name : names) {
                    if (name.trim().equals(mainCategory)) {
                        return m;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取采集项的次分类
     *
     * @param category 次分类
     * @return 采集项的次分类
     */
    public GatherXmlCategoryBean queryCategory(String category) {
        for (GatherXmlMainCategoryBean m : this.mainCategory) {
            if (null == m.getCategory()) {
                continue;
            }
            for (GatherXmlCategoryBean c : m.getCategory()) {
                if (category.equalsIgnoreCase(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }

}
