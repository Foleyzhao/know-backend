package com.cumulus.modules.business.entity;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产采集配置
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetConfig {

    /**
     * 采集方式 多选
     */
    private List<Integer> gatherType;

    /**
     * 远程扫描
     */
    private RemoteScan remoteScan;

    /**
     * 登录采集
     */
    private LoginGather login;

}

