package com.cumulus.modules.mnt.service;

import com.cumulus.modules.mnt.entity.ServerDeploy;
import com.cumulus.modules.mnt.dto.ServerDeployDto;
import com.cumulus.modules.mnt.dto.ServerDeployQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 服务器服务接口
 */
public interface ServerDeployService {

    /**
     * 分页查询服务器
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 服务器列表
     */
    Object queryAll(ServerDeployQueryCriteria criteria, Pageable pageable);

    /**
     * 查询服务器
     *
     * @param criteria 查询参数
     * @return 服务器列表
     */
    List<ServerDeployDto> queryAll(ServerDeployQueryCriteria criteria);

    /**
     * 根据ID查询服务器
     *
     * @param id 服务器ID
     * @return 服务器
     */
    ServerDeployDto findById(Long id);

    /**
     * 创建服务器
     *
     * @param resources 服务器
     */
    void create(ServerDeploy resources);

    /**
     * 编辑服务器
     *
     * @param resources 服务器
     */
    void update(ServerDeploy resources);

    /**
     * 删除服务器
     *
     * @param ids 服务器ID集合
     */
    void delete(Set<Long> ids);

    /**
     * 根据IP查询服务器
     *
     * @param ip IP
     * @return 服务器
     */
    ServerDeployDto findByIp(String ip);

    /**
     * 测试登录服务器
     *
     * @param resources 服务器
     * @return 结果
     */
    Boolean testConnect(ServerDeploy resources);

    /**
     * 导出数据
     *
     * @param queryAll 服务器列表
     * @param response 响应
     * @throws IOException 异常
     */
    void download(List<ServerDeployDto> queryAll, HttpServletResponse response) throws IOException;
}
