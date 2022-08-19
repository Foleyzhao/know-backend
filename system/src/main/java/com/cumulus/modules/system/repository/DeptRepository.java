package com.cumulus.modules.system.repository;

import com.cumulus.modules.system.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 系统部门数据访问接口
 */
public interface DeptRepository extends JpaRepository<Dept, Long>, JpaSpecificationExecutor<Dept> {

    /**
     * 根据父部门ID查询子部门列表
     *
     * @param id 父部门ID
     * @return 子部门列表
     */
    List<Dept> findByPid(Long id);

    /**
     * 获取顶级部门
     *
     * @return 顶级部门列表
     */
    List<Dept> findByPidIsNull();

    /**
     * 根据部门ID获取子部门数量
     *
     * @param pid 部门ID
     * @return 子部门数量
     */
    int countByPid(Long pid);

    /**
     * 根据部门ID更新子部门数量
     *
     * @param count 子部门数量
     * @param id    部门ID
     */
    @Modifying
    @Query(value = "update sys_dept set sub_count = ?1 where id = ?2 ", nativeQuery = true)
    void updateSubCntById(Integer count, Long id);

    /**
     * 根据部门名称查找部门列表 （正常只会有一个）
     *
     * @param name 部门名称
     * @return 返回部门列表
     */
    List<Dept> findAllByName(@NotBlank String name);
}
