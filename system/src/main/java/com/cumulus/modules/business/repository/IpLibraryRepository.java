package com.cumulus.modules.business.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import com.cumulus.modules.business.entity.IpLibrary;
import com.cumulus.modules.system.entity.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * ip库数据访问接口
 *
 * @author zhangxq
 */
public interface IpLibraryRepository extends JpaRepository<IpLibrary, Long>, JpaSpecificationExecutor<IpLibrary> {


    /**
     * 根据全写ip查询
     *
     * @return 当前ip个数
     */
    int countByCompleteIpEquals(String completeIp);

    /**
     * 根据ip全写查询ip库
     *
     * @param completeIp
     * @return
     */
    IpLibrary findByCompleteIpEquals(String completeIp);

    /**
     * 修改部门
     *
     * @param deptId 部门ID
     * @param id     ip库id
     */
    @Modifying
    @Query(value = "update tbl_ip_library set dept_id = ?1 where id = ?2 ", nativeQuery = true)
    void updateDept(Long deptId, Long id);

    /**
     * 查询所有ip
     *
     * @return
     */
    @Query(value = "select ip from tbl_ip_library", nativeQuery = true)
    List<String> queryIp();

    /**
     * 根据部门id查询ip
     *
     * @param ids
     * @return
     */
    @Query(value = "select ip from tbl_ip_library where dept_id in ?1", nativeQuery = true)
    List<String> queryIpByDept(Set<Long> ids);

    /**
     * 根据部门查询ip个数
     *
     * @param dept 部门
     * @return 个数
     */
    Integer countAllByDeptIn(Collection<Dept> dept);

    /**
     * 根据部门id查询ip库
     *
     * @param set 部门id
     * @return 数量
     */
    @Query(value = "select count(*) from tbl_ip_library where dept_id in ?1", nativeQuery = true)
    int countNum(Set<String> set);
}
