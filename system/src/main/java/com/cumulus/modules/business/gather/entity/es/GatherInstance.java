package com.cumulus.modules.business.gather.entity.es;

import java.util.List;
import java.util.Map;

public interface GatherInstance {

    /**
     * 设置风险类型
     *
     * @param riskType 风险类型
     */
    void setRiskType(List<Long> riskType);

    /**
     * 设置风险等级
     *
     * @param level 风险等级
     */
    void setRiskLevel(Integer level);

    /**
     * 获取风险等级
     *
     * @return 风险等级
     */
    Integer getRiskLevel();

    /**
     * 获取ID
     *
     * @return ID
     */
    String getId();

    /**
     * 获取详情
     *
     * @return 详情
     */
    Map<String, Object> getDetail();

    /**
     * 记录变化
     */
    void saveChangeContent();

    /**
     * 是否记录变化
     */
    void isSaveChange();


}
