package com.cumulus.modules.mnt.service;

import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.dto.DeployHistoryDto;
import com.cumulus.modules.mnt.dto.DeployHistoryQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 部署历史服务接口
 */
public interface DeployHistoryService {

    /**
     * 分页查询部署历史
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 部署历史列表
     */
    Object queryAll(DeployHistoryQueryCriteria criteria, Pageable pageable);

    /**
     * 查询部署历史
     *
     * @param criteria 查询参数
     * @return 部署历史列表
     */
    List<DeployHistoryDto> queryAll(DeployHistoryQueryCriteria criteria);

    /**
     * 根据ID查询部署历史
     *
     * @param id 部署历史ID
     * @return 部署历史
     */
    DeployHistoryDto findById(String id);

    /**
     * 创建部署历史
     *
     * @param resources 部署历史
     */
    void create(DeployHistory resources);

    /**
     * 删除
     *
     * @param ids 部署历史ID集合
     */
    void delete(Set<String> ids);

    /**
     * 导出数据
     *
     * @param queryAll 部署历史列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<DeployHistoryDto> queryAll, HttpServletResponse response) throws IOException;
}
