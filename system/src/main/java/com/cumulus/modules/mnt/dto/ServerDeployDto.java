package com.cumulus.modules.mnt.dto;

import com.cumulus.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务器传输对象
 */
@Getter
@Setter
public class ServerDeployDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -2374749985929788388L;
    
    /**
     * ID
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        ServerDeployDto that = (ServerDeployDto) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
