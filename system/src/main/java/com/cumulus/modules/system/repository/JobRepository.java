package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

/**
 * 系统岗位数据访问接口
 */
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    /**
     * 根据岗位名称查询岗位
     *
     * @param name 岗位名称
     * @return 岗位
     */
    Job findByName(String name);

    /**
     * 根据岗位Id集合删除岗位
     *
     * @param ids 岗位ID集合
     */
    void deleteAllByIdIn(Set<Long> ids);

}