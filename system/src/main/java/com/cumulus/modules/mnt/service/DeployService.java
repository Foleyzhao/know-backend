package com.cumulus.modules.mnt.service;

import com.cumulus.modules.mnt.entity.Deploy;
import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.dto.DeployDto;
import com.cumulus.modules.mnt.dto.DeployQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 部署服务接口
 */
public interface DeployService {

    /**
     * 分页查询部署
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 部署列表
     */
    Object queryAll(DeployQueryCriteria criteria, Pageable pageable);

    /**
     * 查询部署
     *
     * @param criteria 查询参数
     * @return 部署列表
     */
    List<DeployDto> queryAll(DeployQueryCriteria criteria);

    /**
     * 根据ID查询
     *
     * @param id 部署ID
     * @return 部署
     */
    DeployDto findById(Long id);

    /**
     * 创建部署
     *
     * @param resources 部署
     */
    void create(Deploy resources);


    /**
     * 编辑部署
     *
     * @param resources 部署
     */
    void update(Deploy resources);

    /**
     * 删除部署
     *
     * @param ids 部署ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 部署应用
     *
     * @param fileSavePath 文件路径
     * @param appId        应用ID
     */
    void deploy(String fileSavePath, Long appId);

    /**
     * 查询部署状态
     *
     * @param resources 部署
     * @return 结果
     */
    String serverStatus(Deploy resources);

    /**
     * 启动服务
     *
     * @param resources 部署
     * @return 结果
     */
    String startServer(Deploy resources);

    /**
     * 停止服务
     *
     * @param resources 部署
     * @return 结果
     */
    String stopServer(Deploy resources);

    /**
     * 还原服务
     *
     * @param resources 部署劣势
     * @return 结果
     */
    String serverReduction(DeployHistory resources);

    /**
     * 导出数据
     *
     * @param queryAll 部署列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<DeployDto> queryAll, HttpServletResponse response) throws IOException;
}
