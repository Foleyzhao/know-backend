package com.cumulus.modules.business.gather.repository;

import java.util.List;
import com.cumulus.modules.business.gather.entity.mysql.GatherPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采集任务数据访问接口
 *
 * @author zhangxq
 */
public interface GatherPlanRepository extends JpaRepository<GatherPlan, Long>, JpaSpecificationExecutor<GatherPlan> {

    /**
     * 更新采集计划状态
     *
     * @param ids    采集计划ID集合
     * @param status 状态
     */
    @Modifying
    @Transactional
    @Query(value = "update tbl_gather_plan set status=?2 where id in ?1", nativeQuery = true)
    void updateState(List<Long> ids, Integer status);

    /**
     * 根据计划名称查询
     *
     * @param name 计划名称
     * @return 数量
     */
    Integer countByNameEquals(String name);

}
