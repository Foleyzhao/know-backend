package com.cumulus.modules.system.service;

import com.cumulus.modules.system.dto.JobQueryCriteria;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.dto.JobDto;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统岗位服务接口
 */
public interface JobService {

    /**
     * 根据岗位ID查询
     *
     * @param id 岗位ID
     * @return 岗位
     */
    JobDto findById(Long id);

    /**
     * 创建岗位
     *
     * @param resources 岗位
     */
    void create(Job resources);

    /**
     * 编辑岗位
     *
     * @param resources 岗位
     */
    void update(Job resources);

    /**
     * 根据岗位ID集合删除岗位
     *
     * @param ids 岗位ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 分页查询岗位列表
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 岗位列表
     */
    Map<String, Object> queryAll(JobQueryCriteria criteria, Pageable pageable);

    /**
     * 根据条件查询全部岗位
     *
     * @param criteria 查询参数
     * @return 岗位列表
     */
    List<JobDto> queryAll(JobQueryCriteria criteria);

    /**
     * 导出岗位列表
     *
     * @param queryAll 待导出的岗位列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<JobDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 验证岗位是否被用户关联
     *
     * @param ids 岗位ID集合
     */
    void verification(Set<Long> ids);

}
