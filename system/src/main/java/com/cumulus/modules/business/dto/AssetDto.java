package com.cumulus.modules.business.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import com.cumulus.base.BaseDTO;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.entity.AssetExtend;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.system.dto.UserDto;
import com.cumulus.modules.system.entity.Dept;
import lombok.Getter;
import lombok.Setter;

/**
 * 资产数据传输对象
 *
 * @author zhangxq
 */
@Getter
@Setter
public class AssetDto extends BaseDTO implements Serializable {

    private static final long serialVersionUID = -3724153578521148572L;

    /**
     * ID
     */
    private Long id;

    /**
     * ip
     */
    private String ip;

    /**
     * ip全写
     */
    private String completeIp;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 端口数
     */
    private Long portNum;

    /**
     * 资产名称
     */
    private String name;

    /**
     * 资产类型
     */
    private AssetType assetType;

    /**
     * 资产类型
     */
    private AssetSysType assetSysType;

    /**
     * 资产类别 主机 应用
     */
    private Integer assetCategory;

    /**
     * 资产状态 0 未开始 1 执行中 2 成功 3 失败
     */
    private Integer assetStatus;

    /**
     * 所属部门
     */
    private Dept dept;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 资产标签
     */
    private Set<AssetTag> assetTags;

    /**
     * 父资产
     */
    private Asset parent;

    /**
     * 扩展属性
     */
    private AssetExtend assetExtend;

    /**
     * ip下端口列表
     */
    private List<Integer> portList;

    /**
     * 导入结果
     */
    private String result;

    /**
     * 父类型名称 导入用
     */
    private String parentTypeName;

    /**
     * 类型名称 导入用
     */
    private String typeName;

    /**
     * 部门名称 导入用
     */
    private String deptName;

    /**
     * 类型名称 导入用
     */
    private String parentTagName;

    /**
     * 标签名称 导入用
     */
    private String tagName;

    /**
     * 登录测试状态 0 未开始 1 执行中 2 成功 3 失败
     */
    private Integer loginStatus;

    /**
     * 风险等级 0 安全 1 低危 2 中危 3 高危
     */
    private Integer riskLevel;

    /**
     * 采集方式 1-Agent 2-登录采集 3-无访问
     */
    private Integer gatherType;

    /**
     * 子资产
     */
    private List<AssetDto> children;

    /**
     * 资产采集配置
     */
    private AssetConfig assetConfig;

    /**
     * web地址
     */
    private String webAddress;

    /**
     * 部门负责人
     */
    private UserDto deptHead;

}
