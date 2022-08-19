package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

/**
 * 系统字典数据访问接口
 */
public interface DictRepository extends JpaRepository<Dict, Long>, JpaSpecificationExecutor<Dict> {

    /**
     * 删除系统字典
     *
     * @param ids 字典ID列表
     */
    void deleteByIdIn(Set<Long> ids);

    /**
     * 查询系统字典
     *
     * @param ids 字典ID列表
     * @return 字典列表
     */
    List<Dict> findByIdIn(Set<Long> ids);

}