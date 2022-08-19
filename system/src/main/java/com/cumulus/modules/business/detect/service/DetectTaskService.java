package com.cumulus.modules.business.detect.service;

import java.util.Set;
import com.cumulus.modules.business.detect.dto.DetectTaskDto;
import com.cumulus.modules.business.detect.dto.DetectTaskQueryCriteria;
import org.springframework.data.domain.Pageable;

/**
 * 发现任务服务接口
 *
 * @author zhangxq
 */
public interface DetectTaskService {

    /**
     * 查询发现任务
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 发现任务列表
     */
    Object queryAll(DetectTaskQueryCriteria criteria, Pageable pageable);

    /**
     * 新增发现任务
     *
     * @param detectTaskDto 发现任务传输对象
     */
    void create(DetectTaskDto detectTaskDto);

    /**
     * 根据id删除
     *
     * @param id 任务id
     */
    void removeById(Long id);

    /**
     * 批量删除
     *
     * @param ids    任务id列表
     * @param delAll 是否删除全部
     */
    void removeBatch(Set<Long> ids, Boolean delAll);


    /**
     * 根据id修改
     *
     * @param detectTaskDto 发现任务数据传输对象
     */
    void updateById(DetectTaskDto detectTaskDto);

    /**
     * 单个执行发现任务
     *
     * @param id 任务id;
     */
    void execute(Long id);

    /**
     * 批量执行发现任务
     *
     * @param ids 任务id列表;
     */
    void execute(Set<Long> ids,Boolean all);

    /**
     * 暂停/继续
     *
     * @param id 任务id
     */
    void pause(Long id);

    /**
     * 取消任务
     *
     * @param id 任务id
     */
    void cancel(Long id);

    /**
     * 根据名称查重
     *
     * @param name 名称
     * @return true不重复 false重复
     */
    boolean checkName(String name);

    /**
     * 判断ip数量
     *
     * @param detectTaskDto 发现任务
     * @return 结果
     */
    String isCreate(DetectTaskDto detectTaskDto);

}
