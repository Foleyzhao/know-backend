package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.DictDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 系统字典详情数据访问接口
 */
public interface DictDetailRepository extends JpaRepository<DictDetail, Long>, JpaSpecificationExecutor<DictDetail> {

    /**
     * 根据字典名称查询字典详情
     *
     * @param name 字典名称
     * @return 字典详情列表
     */
    List<DictDetail> findByDictName(String name);

}