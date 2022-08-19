package com.cumulus.modules.mnt.repository;

import com.cumulus.modules.mnt.entity.ServerDeploy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 服务器数据访问接口
 */
public interface ServerDeployRepository extends JpaRepository<ServerDeploy, Long>,
        JpaSpecificationExecutor<ServerDeploy> {

    /**
     * 根据IP查询服务器
     *
     * @param ip IP
     * @return 服务器
     */
    ServerDeploy findByIp(String ip);
}
