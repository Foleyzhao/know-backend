package com.cumulus.modules.mnt.dto;

import cn.hutool.core.collection.CollectionUtil;
import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部署传输对象
 */
@Getter
@Setter
public class DeployDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -5443733731312260318L;

    /**
     * ID
     */
    private String id;

    /**
     * 应用
     */
    private AppDto app;

    /**
     * 服务器集合
     */
    private Set<ServerDeployDto> deploys;

    /**
     * 服务器列表字符串
     */
    private String servers;

    /**
     * 服务状态
     */
    private String status;

    /**
     * 获取服务器列表字符串
     *
     * @return 服务器列表字符串
     */
    public String getServers() {
        if (CollectionUtil.isNotEmpty(deploys)) {
            return deploys.stream().map(ServerDeployDto::getName).collect(Collectors.joining(","));
        }
        return servers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        DeployDto deployDto = (DeployDto) o;
        return Objects.equals(id, deployDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
