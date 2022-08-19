package com.cumulus.modules.business.gather.service;

import java.util.Optional;
import java.util.Set;
import com.cumulus.modules.business.gather.dto.GatherTaskDto;
import com.cumulus.modules.business.gather.dto.GatherTaskQueryCriteria;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import org.springframework.data.domain.Pageable;

/**
 * 采集计划服务接口
 *
 * @author zhangxq
 */
public interface GatherPlanService {

    /**
     * 查询采集任务
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 采集任务列表
     */
    Object queryAll(GatherTaskQueryCriteria criteria, Pageable pageable);

    /**
     * 新增采集任务
     *
     * @param gatherTaskDto 采集任务传输对象
     */
    void create(GatherTaskDto gatherTaskDto);

    /**
     * 根据id删除
     *
     * @param id
     */
    void removeById(Long id);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    void removeBatch(Set<Long> ids, Boolean delAll);

    /**
     * 根据id修改
     *
     * @param gatherTaskDto 发现任务传输对象
     */
    void updateById(GatherTaskDto gatherTaskDto);

    /**
     * 根据id开始任务
     *
     * @param id
     */
    void start(Long id);

    /**
     * 根据id开始任务
     *
     * @param id 资产ID
     */
    void startByAssetId(Long id);

    /**
     * 批量开始
     *
     * @param ids
     * @param startAll
     */
    void startBatch(Set<Long> ids, Boolean startAll);

    /**
     * 暂停
     *
     * @param id
     */
    void pause(Long id);

    /**
     * 取消
     *
     * @param id
     */
    void cancel(Long id);

    /**
     * 判断并更新采集计划执行状态
     *
     * @param planId     采集计划ID
     * @param taskStatus 采集任务执行状态（true-开始执行，false-结束执行）
     */
    void judgeAndUpdatePlanStatus(Long planId, boolean taskStatus);

    /**
     * 更新采集计划状态
     *
     * @param planId 采集计划ID
     * @param state  状态
     */
    void updatePlanState(Long planId, Integer state);

    /**
     * 根据id 获取 GatherPlan 获取lazy的AssetList
     *
     * @param id 主键id
     * @return 返回对象
     */
    Optional<GatherPlan> findByIdHasAssetList(Long id);

    /**
     * 根据名称查重
     *
     * @param name
     * @return
     */
    boolean checkName(String name);

    /**
     * 根据任务id查询资产列表
     *
     * @param id       id
     * @param pageable 分页
     * @return 资产列表
     */
    Object findAssetsById(Long id, Pageable pageable);

    /**
     * 判断ip数量
     *
     * @param gatherTaskDto 计划
     * @return 结果
     */
    String isCreate(GatherTaskDto gatherTaskDto);
}
