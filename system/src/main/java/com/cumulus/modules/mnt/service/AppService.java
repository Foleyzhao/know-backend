package com.cumulus.modules.mnt.service;

import com.cumulus.modules.mnt.entity.App;
import com.cumulus.modules.mnt.dto.AppDto;
import com.cumulus.modules.mnt.dto.AppQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 应用服务接口
 */
public interface AppService {

    /**
     * 分页查询应用
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 应用列表
     */
    Object queryAll(AppQueryCriteria criteria, Pageable pageable);

    /**
     * 查询应用
     *
     * @param criteria 查询参数
     * @return 应用列表
     */
    List<AppDto> queryAll(AppQueryCriteria criteria);

    /**
     * 根据ID查询应用
     *
     * @param id 应用ID
     * @return 应用
     */
    AppDto findById(Long id);

    /**
     * 创建应用
     *
     * @param resources 应用
     */
    void create(App resources);

    /**
     * 编辑应用
     *
     * @param resources 应用
     */
    void update(App resources);

    /**
     * 删除应用
     *
     * @param ids 应用ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 导出数据
     *
     * @param queryAll 应用列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<AppDto> queryAll, HttpServletResponse response) throws IOException;
}
