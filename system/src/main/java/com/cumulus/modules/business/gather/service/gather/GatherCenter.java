package com.cumulus.modules.business.gather.service.gather;

import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.common.service.FileChangeService;
import com.cumulus.modules.business.gather.common.utils.CommUtils;
import com.cumulus.modules.business.gather.model.GatherDetail;
import com.cumulus.modules.business.gather.model.GatherGroup;
import com.cumulus.modules.business.gather.model.GatherXmlBean;
import com.cumulus.modules.business.gather.model.GatherXmlCategoryBean;
import com.cumulus.modules.business.gather.model.GatherXmlMainCategoryBean;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * (采集)指标维护类
 *
 * @author zhaoff
 */
@Getter
@Setter
@Slf4j
@Component(value = "gatherCenter")
public class GatherCenter {

    /**
     * 文件变更监控服务
     */
    @Autowired
    private FileChangeService fileChangeService;

    /**
     * 采集配置文件对应的实体
     */
    private GatherXmlBean dataCenter;

    /**
     * 采集配置文件存放的目录
     */
    public static final String gatherConfigDir = CommUtils.getDataDir().concat(File.separator).concat("gather")
            .concat(File.separator);

    /**
     * 当采集的原始输出太长的时候，采集结果放到文本文件中存放的目录
     */
    public static final String gatherLogDir = CommUtils.getDataDir().concat(File.separator).concat("share")
            .concat(File.separator).concat("gather").concat(File.separator).concat("output").concat(File.separator);

    /**
     * 所有资产类型对应的采集任务类型
     */
    private final Map<Integer, Set<Integer>> collectTypesBySysType = new HashMap<>();

    /**
     * 初始化
     */
    @PostConstruct
    private void init() {
        File logsDir = new File(GatherCenter.gatherLogDir);
        if (!logsDir.exists()) {
            if (!logsDir.mkdirs() && log.isDebugEnabled()) {
                log.debug("Mkdirs " + logsDir.getAbsolutePath() + " failed");
            }
        }
        File watchFile = new File(gatherConfigDir);
        if (!watchFile.exists()) {
            if (log.isErrorEnabled()) {
                log.error("Gather config dir does not exists:" + watchFile.getAbsolutePath());
            }
            return;
        }
        // 加载采集脚本
        loadGatherScript(watchFile);
        // 增加文件监听
        fileChangeService.addFileChangeMonitor(watchFile);
        fileChangeService.addFileChangeListener((kind, file) -> {
            if (file.getParentFile().equals(watchFile)) {
                // 加载采集脚本
                loadGatherScript(watchFile);
            }
        });
    }

    /**
     * 加载采集脚本
     *
     * @param gatherFile 采集脚本存放目录
     */
    void loadGatherScript(File gatherFile) {
        dataCenter = new GatherXmlBean();
        dataCenter.setMainCategory(new ArrayList<>());
        File[] files = gatherFile.listFiles();
        if (null != files) {
            for (File file : files) {
                buildDataCenter(file);
            }
        }
        // 排序采集主分类
        dataCenter.buildComplete();
    }

    /**
     * 根据指定文件构建指标项
     *
     * @param file 指定的文件
     */
    private void buildDataCenter(File file) {
        try {
            if (!file.getName().endsWith(".xml")) {
                return;
            }
            JAXBContext ctx = JAXBContext.newInstance(GatherXmlMainCategoryBean.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            GatherXmlMainCategoryBean mainCategory = (GatherXmlMainCategoryBean) unmarshaller.unmarshal(file);
            dataCenter.merge(mainCategory);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Parse gather xml file " + file.getAbsolutePath() + " failed", e);
            }
        }
    }

    /**
     * 向资产添加采集类型
     *
     * @param assetType      资产类型
     * @param collectTypeSet 支持的采集类型
     */
    private void addCollectTypeToAssetType(Integer assetType, Set<Integer> collectTypeSet) {
        Set<Integer> tmp = new HashSet<>(collectTypeSet);
        collectTypesBySysType.put(assetType, tmp);
        collectTypeSet.clear();
    }

    /**
     * 覆盖写文件，不存在则创建
     *
     * @param fileName 文件名
     * @param content  要写入的内容
     */
    private void writeFile(String fileName, String content) {
        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            fos = new FileOutputStream(fileName);
            pw = new PrintWriter(fos);
            pw.write(content);
            pw.flush();
        } catch (FileNotFoundException e) {
            if (log.isErrorEnabled()) {
                log.error("Write file " + fileName + ":" + e);
            }
        } finally {
            try {
                if (null != fos) {
                    fos.close();
                }
                if (null != pw) {
                    pw.close();
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Close file " + fileName + ":" + e);
                }
            }
        }
    }

    /**
     * 根据主类次类获取采集项分组列表
     *
     * @param mainCategory 主类
     * @param category     次类
     * @return 采集项分组列表
     */
    public List<GatherGroup> getGatherIndicators(String mainCategory, String category) {
        try {
            return getGroups(mainCategory, category);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("getBaselineIndicators failed", e);
            }
            return null;
        }
    }

    /**
     * 根据主类次类获取所有采集项信息
     *
     * @param mainCategory 主类
     * @param category     次类
     * @return 所有采集项信息
     */
    public List<GatherDetail> getIndicatorsByMainAndCate(String mainCategory, String category) {
        List<GatherDetail> details = new ArrayList<>();
        if (null == mainCategory || null == category) {
            return details;
        }
        try {
            List<GatherGroup> groups = getGroups(mainCategory, category);
            if (null == groups) {
                throw new NullPointerException();
            }
            for (GatherGroup group : groups) {
                getIndicatorsInGroup(details, group);
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("failed to get indicators  mainCategory:%s, category:%s.", mainCategory,
                        category), e);
            }
        }
        return details;
    }

    /**
     * 根据主类、次类、第一级采集分组名称获取所有采集项信息
     *
     * @param mainCategory 主类
     * @param category     次类
     * @param firstGroup   第一级采集分组名称
     * @return 所有采集项信息
     */
    public List<GatherDetail> getIndicatorsByGroup(String mainCategory, String category, String firstGroup) {
        if (null != firstGroup) {
            firstGroup = firstGroup.toLowerCase();
        }
        List<GatherDetail> details = new ArrayList<>();
        if (null == mainCategory || null == category || null == firstGroup) {
            return details;
        }
        try {
            List<GatherGroup> groups = getGroups(mainCategory, category);
            if (null == groups) {
                throw new NullPointerException();
            }
            for (GatherGroup group : groups) {
                if (group.getName().contains(firstGroup)) {
                    getIndicatorsInGroup(details, group);
                    break;
                }
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(String.format("Failed to get indicators mainCategory:%s, category:%s, group:%s.", mainCategory,
                        category, firstGroup), e);
            }
        }
        return details;
    }

    /**
     * 递归获取分组下的采集项信息
     *
     * @param list  采集项的详情列表
     * @param group 采集分组信息
     */
    public void getIndicatorsInGroup(List<GatherDetail> list, GatherGroup group) {
        if (null != group) {
            if (null != group.getIndicator()) {
                list.addAll(group.getIndicator());
            }
            if (null != group.getGroup() && group.getGroup().size() > 0) {
                for (GatherGroup gro : group.getGroup()) {
                    getIndicatorsInGroup(list, gro);
                }
            }
        }
    }

    /**
     * 根据item获取采集项主类显示名称
     *
     * @param itemKey itemKey
     * @return 采集项主类显示名称
     */
    public String getMainCategoryGroup(String itemKey) {
        GatherXmlMainCategoryBean main = this.dataCenter.queryMainCategoryByItemKey(itemKey);
        if (null != main) {
            return main.getDisplay();
        }
        return null;
    }

    /**
     * 根据显示名称获取采集项主类
     *
     * @param display 显示名称
     * @return 采集项主类
     */
    public List<GatherXmlMainCategoryBean> getMainCategoryByDisplay(String display) {
        return this.dataCenter.queryMainCategoryByDisplay(display);
    }

    /**
     * 根据itemKey获取此采集项的优先级
     *
     * @param itemKey itemKey
     * @return 采集项的优先级
     */
    public Integer getMainCategoryPriority(String itemKey) {
        GatherXmlMainCategoryBean main = this.dataCenter.queryMainCategoryByItemKey(itemKey);
        if (null != main) {
            return main.getPriority();
        }
        return -1;
    }

    /**
     * 根据itemKey获取采集项分组
     *
     * @param itemKey itemKey
     * @return 采集项分组
     */
    public GatherGroup getIndicatorGroup(String itemKey) {
        GatherDetail indicatorDetail = this.dataCenter.queryBaselineDetail(itemKey);
        if (null != indicatorDetail) {
            return indicatorDetail.getParentGroup();
        }
        return null;
    }

    /**
     * 根据itemKey获取采集项组父分组
     *
     * @param itemKey itemKey
     * @return 采集项组父分组
     */
    public GatherGroup getAncestorGroup(String itemKey) {
        GatherDetail indicatorDetail = this.dataCenter.queryBaselineDetail(itemKey);
        if (null != indicatorDetail) {
            return indicatorDetail.getAncestorGroup();
        }
        return null;
    }

    /**
     * 根据itemKey获取采集项组父分组显示名称
     *
     * @param itemKey itemKey
     * @return 采集组父分组显示名称
     */
    public String getAncestorDisplay(String itemKey) {
        GatherGroup group = getAncestorGroup(itemKey);
        if (null != group) {
            return group.getDisplay();
        }
        return null;
    }

    /**
     * 根据itemKey获取采集项的详情
     *
     * @param itemKey itemKey
     * @return 采集项的详情
     */
    public GatherDetail getGatherDetail(String itemKey) {
        return this.dataCenter.queryBaselineDetail(itemKey);
    }

    /**
     * 采集项的详情
     *
     * @param mainCategory 主类
     * @param category     次类
     * @param key          itemKey
     * @return 采集项详情
     */
    public GatherDetail getGatherDetail(String mainCategory, String category, String key) {
        try {
            List<GatherGroup> groups = getGroups(mainCategory, category);
            String[] keyNames = key.split("\\.");
            if (keyNames.length < 2) {
                return null;
            }
            GatherGroup parentGroup = null;
            if (null == groups) {
                throw new NullPointerException();
            }
            for (GatherGroup group : groups) {
                if (group.getName().equals(keyNames[0])) {
                    parentGroup = group;
                    break;
                }
            }
            if (null == parentGroup) {
                if (!key.contains("_prerequisite")) {
                    return null;
                }
            }
            for (int i = 1; i < keyNames.length - 1; i++) {
                boolean found = false;
                if (null != parentGroup) {
                    for (GatherGroup group : parentGroup.getGroup()) {
                        if (group.getName().equals(keyNames[i])) {
                            parentGroup = group;
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    return null;
                }
            }
            if (null == parentGroup) {
                throw new NullPointerException();
            }
            for (GatherDetail indicator : parentGroup.getIndicator()) {
                if (indicator.getName().equals(keyNames[keyNames.length - 1])) {
                    return indicator;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 根据主类和次类获取采集项的次分类（用于页面展示）
     *
     * @param mainCategory 主类
     * @param category     次类
     * @return 采集项的次分类
     */
    public GatherXmlCategoryBean getGatherCategory(String mainCategory, String category) {
        try {
            return getCategory(mainCategory, category);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("GetIndicators failed", e);
            }
            return null;
        }
    }

    /**
     * 获取所有采集项列表
     *
     * @return 采集项项集合
     */
    public Map<String, GatherDetail> getAllGatherDetails() {
        Map<String, GatherDetail> map = new HashMap<>();
        for (GatherXmlMainCategoryBean m : dataCenter.getMainCategory()) {
            for (GatherXmlCategoryBean s : m.getCategory()) {
                for (GatherGroup group : s.getGroup()) {
                    takeDetailsInGroup(group, null, map);
                }
            }
        }
        return map;
    }

    /**
     * 递归从采集项分组中获取采集项
     *
     * @param group      采集项分组
     * @param parentName 父分组名称
     * @param map        采集项集合
     */
    private void takeDetailsInGroup(GatherGroup group, String parentName, Map<String, GatherDetail> map) {
        if (null == parentName) {
            parentName = group.getName();
        } else {
            parentName = parentName + "." + group.getName();
        }
        List<GatherDetail> detailList = group.getIndicator();
        if (null != detailList && detailList.size() > 0) {
            for (GatherDetail detail : detailList) {
                String name = parentName + "." + detail.getName();
                map.put(name, detail);
            }
        }
        List<GatherGroup> groupList = group.getGroup();
        if (null != groupList && groupList.size() > 0) {
            for (GatherGroup child : groupList) {
                takeDetailsInGroup(child, parentName, map);
            }
        }
    }

    /**
     * 根据主类别、次类别和采集项key获取采集项的名称
     *
     * @param mainCategory 主类
     * @param category     次类
     * @param key          采集项key
     * @return 采集项的名称
     */
    public String getDisplayNameByKey(String mainCategory, String category, String key) {
        GatherDetail detail = getGatherDetail(mainCategory, category, key);
        if (null == detail) {
            return null;
        }
        return detail.getDisplay();
    }

    /**
     * 根据采集项key查找预采集项key
     *
     * @param mainCategory 主类
     * @param category     次类
     * @param key          采集项key
     * @return 预采集项key
     */
    public String getPreKey(String mainCategory, String category, String key) {
        GatherDetail detail = getGatherDetail(mainCategory, category, key);
        if (null == detail) {
            detail = getGatherDetail(key);
        }
        if (null == detail) {
            return null;
        }
        String require = detail.getRequire();
        if (null == require) {
            return null;
        }
        return "_prerequisite." + require;
    }

    /**
     * 根据采集项key查找预采集项key列表
     *
     * @param mainCategory 主类
     * @param category     次类
     * @param key          采集项key
     * @return 预采集项key列表
     */
    public List<String> getPreKeys(String mainCategory, String category, String key) {
        ArrayList<String> preKeys = new ArrayList<>();
        GatherDetail detail = getGatherDetail(mainCategory, category, key);
        if (null == detail) {
            detail = getGatherDetail(key);
        }
        if (null == detail) {
            return preKeys;
        }
        String require = detail.getRequire();
        if (null == require) {
            return preKeys;
        }
        String[] temps = require.split(",");
        for (String temp : temps) {
            preKeys.add("_prerequisite." + temp.trim());
        }
        return preKeys;
    }

    /**
     * 根据主类别、次类别、分组名获取第一层分组
     *
     * @param mainCategory 主类别
     * @param category     次类别
     * @param groupName    分组名
     * @return 第一层分组
     */
    public GatherGroup getFirstLevelGroup(String mainCategory, String category, String groupName) {
        GatherGroup result = null;
        try {
            List<GatherGroup> groups = getGroups(mainCategory, category);
            if (null == groups) {
                throw new NullPointerException();
            }
            for (GatherGroup group : groups) {
                if (group.getName().equalsIgnoreCase(groupName)) {
                    result = group;
                    break;
                }
            }
        } catch (Exception e) {
            mainCategory = null != mainCategory ? mainCategory : "";
            category = null != category ? category : "";
            groupName = null != groupName ? groupName : "";
            if (log.isDebugEnabled()) {
                String errMsg = "Failed to get first level group, name: %s, category:%s, mainCategory:%s with %s.";
                log.debug(String.format(errMsg, groupName, category, mainCategory, e));
            }
        }
        return result;
    }

    /**
     * 根据主类别和次类别获取所有采集项分组
     *
     * @param mainCategory 主类别
     * @param category     次类别
     * @return 所有采集项分组
     */
    private List<GatherGroup> getGroups(String mainCategory, String category) {
        try {
            // 次类别
            GatherXmlCategoryBean secondCategoryBean = getCategory(mainCategory, category);
            if (null == secondCategoryBean) {
                return null;
            }
            return secondCategoryBean.getGroup();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据主类名和次类名获取采集项的次分类
     *
     * @param mainCategory 主类
     * @param category     次类
     * @return 采集项的次分类
     */
    public GatherXmlCategoryBean getCategory(String mainCategory, String category) {
        try {
            GatherXmlMainCategoryBean mainCategoryBean = dataCenter.queryMainCategory(mainCategory);
            if (null == mainCategoryBean) {
                return null;
            }
            // 次类别
            return mainCategoryBean.queryCategory(category);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据主类名和次类名获取采集项的次分类列表
     *
     * @param mainCategory 主类
     * @param category     次类
     * @return 采集项的次分类列表
     */
    public List<GatherXmlCategoryBean> getCategoryList(String mainCategory, String category) {
        try {
            GatherXmlMainCategoryBean mainC = dataCenter.queryMainCategory(mainCategory);
            if (null == mainC) {
                return null;
            }
            // 次类别
            return mainC.queryCategoryList(category);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据次类名判断是否存在该次分类的采集
     *
     * @param category 次类名称
     * @return 是否存在该次分类的采集
     */
    public boolean isExistCategory(String category) {
        List<GatherXmlMainCategoryBean> list = dataCenter.getMainCategory();
        for (GatherXmlMainCategoryBean main : list) {
            for (GatherXmlCategoryBean cate : main.getCategory()) {
                if (cate.getName().equals(category)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取itemKey和主分类映射关系
     *
     * @return itemKey和主分类映射关系
     */
    public Map<String, GatherXmlMainCategoryBean> queryIndicatorMainMap() {
        return dataCenter.getIndicatorMainMap();
    }

    /**
     * 获取itemKey和采集项的详情映射关系
     *
     * @return itemKey和采集项的详情映射关系
     */
    public Map<String, GatherDetail> queryIndicatorMap() {
        return dataCenter.getIndicatorMap();
    }

    /**
     * 根据资产类型id获取其对应的采集类型
     *
     * @param sysTypeId 资产类型id
     * @return 采集类型集合
     */
    public Set<Integer> getCollectTypesBySysType(Integer sysTypeId) {
        Set<Integer> assetTaskTypes = collectTypesBySysType.get(sysTypeId);
        if (null == assetTaskTypes) {
            assetTaskTypes = new HashSet<>();
            // TODO zhaoff 初始化时生成资产类型和采集类型对应的关系（默认只有TYPE_SELDOM_ITEM），暂时临时处理
            assetTaskTypes.add(GatherConstants.TYPE_SELDOM_ITEM);
            assetTaskTypes.add(GatherConstants.TYPE_FREQUENTLY_ITEM);
            assetTaskTypes.add(GatherConstants.TYPE_STATIONARY_ITEM);
        }
        return assetTaskTypes;
    }
}
