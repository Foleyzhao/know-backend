package com.cumulus.modules.system.service;

import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.dto.DeptDto;
import com.cumulus.modules.system.dto.DeptQueryCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 系统部门服务接口
 *
 * @author shenjc
 */
public interface DeptService {

    /**
     * 查询所有数据
     *
     * @param criteria 查询参数
     * @param isQuery  是否是查询
     * @return 部门列表
     * @throws Exception 异常
     */
    List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 查询所有数据
     *
     * @param criteria 查询参数
     * @param pageable 分页信息
     * @return 部门列表
     */
    Page<DeptDto> queryAll(DeptQueryCriteria criteria, Pageable pageable);

    /**
     * 根据部门ID查询部门
     *
     * @param id 部门ID
     * @return 部门
     */
    DeptDto findById(Long id);

    /**
     * 创建部门
     *
     * @param resources 部门
     */
    void create(Dept resources);

    /**
     * 编辑部门
     *
     * @param resources 部门
     */
    void update(Dept resources);

    /**
     * 根据部门集合删除部门
     *
     * @param deptDtos 部门集合
     */
    void delete(Set<DeptDto> deptDtos);

    /**
     * 根据父部门ID查询部门列表
     *
     * @param pid 父部门ID
     * @return 部门列表
     */
    List<Dept> findByPid(long pid);

    /**
     * 导出部门数据
     *
     * @param queryAll 待导出的数据
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<DeptDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 根据删除的部门递归获取待删除的部门
     *
     * @param deptList 删除部门列表
     * @param deptDtos 待删除部门集合
     * @return 待删除部门集合
     */
    Set<DeptDto> getDeleteDepts(List<Dept> deptList, Set<DeptDto> deptDtos);

    /**
     * 递归获取获取同级与上级部门
     *
     * @param deptDto 部门
     * @param depts   同级与上级部门列表
     * @return 同级与上级部门列表
     */
    List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts);

    /**
     * 构建部门树形数据
     *
     * @param deptDtos 部门列表
     * @return 部门列表
     */
    Object buildTree(List<DeptDto> deptDtos);

    /**
     * 获取子部门列表
     *
     * @param deptList 部门列表
     * @return 部门ID列表
     */
    List<Long> getDeptChildren(List<Dept> deptList);

    /**
     * 验证是否被角色或用户关联
     *
     * @param deptDtos 部门集合
     */
    void verification(Set<DeptDto> deptDtos);

    /**
     * 根据部门名称获取部门
     *
     * @param deptName 部门名
     * @return 返回部门
     */
    Optional<Dept> findByDeptName(String deptName);
}
