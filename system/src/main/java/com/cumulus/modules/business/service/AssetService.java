package com.cumulus.modules.business.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.dto.AssetQueryCriteria;
import com.cumulus.modules.business.dto.AssetWarehouseDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.gather.vo.AssetPortraitVo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产服务接口
 *
 * @author zhangxq
 */
public interface AssetService {

    /**
     * 查询资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    Object queryAll(AssetQueryCriteria criteria, Pageable pageable);

    /**
     * 查询子资产
     *
     * @param pid      父id
     * @param pageable 分页参数
     * @return 资产列表
     */
    Object queryChild(Long pid, Pageable pageable);

    /**
     * 资产清单 新增资产
     *
     * @param assetDto 资产传输对象
     */
    AssetDto create(AssetDto assetDto, boolean isBatch);

    /**
     * 资产清单 新增资产
     *
     * @param assetDto 资产传输对象
     */
    AssetDto createByAssetConfirm(AssetDto assetDto, boolean isBatch);

    /**
     * 批量新增资产
     *
     * @param file      资产传输对象
     * @param fromAsset 判断入口是否为资产清单
     * @return 资产传输对象
     */
    Object createBatch(MultipartFile file, boolean fromAsset);

    /**
     * 查询ip相关资产
     *
     * @param assetIp 资产ip
     * @return true 无 false 有
     */
    List<AssetDto> queryByCompleteIp(String assetIp);

    /**
     * 根据id删除
     *
     * @param id
     */
    void removeById(Long id);

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    void removeBatch(Set<Long> ids, Boolean delAll);

    /**
     * 根据id修改
     *
     * @param assetDtos 资产传输对象
     */
    void updateById(List<AssetDto> assetDtos);

    /**
     * 登录测试
     *
     * @param ids
     * @return
     */
    Object loginTest(Set<Long> ids, boolean all);

    /**
     * 取消
     *
     * @param id
     */
    void cancel(Long id);

    /**
     * 根据资产ID查找资产
     *
     * @param id 资产ID
     * @return 资产
     */
    Asset findById(Long id);

    /**
     * 根据资产ID查找es资产
     *
     * @param id 资产ID
     * @return 资产
     */
    AssetPortraitVo findGatherById(Long id);

    /**
     * 资产画像展示的Tab页
     *
     * @param id 资产ID
     * @return 结果集
     */
    List<String> getHeader(Long id);

    /**
     * 更新资产的资产采集状态
     *
     * @param assetId     资产id
     * @param assetStatus 资产状态
     */
    void updateAssetStatus(Long assetId, Integer assetStatus);

    /**
     * 复杂分页查询列表 父资产
     *
     * @param asset    查询条件
     * @param pageable 分页条件
     * @return 分页对象
     */
    Page<AssetWarehouseDto> findList(AssetQueryCriteria asset, Pageable pageable);

    /**
     * 复杂分页查询列表 子资产
     *
     * @param asset    查询条件
     * @param pageable 分页条件
     * @return 分页对象
     */
    Page<Asset> findChildList(AssetQueryCriteria asset, Pageable pageable);

    /**
     * 查询全部资产计数 （全部 安全 低位 中危 高危 存活 下线 异常）
     *
     * @return map对象
     */
    Map<String, Long> assetCount();

    /**
     * 根据id更新某个资产的采集ID
     *
     * @param id            资产Id
     * @param gatherAssetId 资产采集Id
     */
    void updateGatherAssetIdById(Long id, String gatherAssetId);

    /**
     * 登录测试
     *
     * @param id
     */
    void login(Long id);

    /**
     * 根据ip获取主键资产 assetCategory = 1
     *
     * @param ip ip
     * @return 返回资产对象
     */
    Asset getHostAssetByIp(String ip);

    /**
     * 根据ip 和端口 获取应用资产 assetCategory = 2
     *
     * @param ip   ip
     * @param port 端口
     * @return 返回资产对象
     */
    Asset getWebAssetByIpAndPort(String ip, Integer port);

    /**
     * 根据ip 和端口 获取应用资产 assetCategory = 2
     *
     * @param webSite 网址
     * @return 返回资产对象
     */
    Asset getWebAssetByWebSite(String webSite);

    /**
     * 根据名称查重
     *
     * @param name 名称
     * @return true不重复 false重复
     */
    boolean checkName(String name);

    /**
     * 资产变更频次
     *
     * @param dateType      时间
     * @param assetCategory 资产类型
     * @return 结果集
     */
    List<Map<String, Long>> countAssetUpdate(Integer dateType, Integer assetCategory);

    /**
     * 资产变更
     *
     * @return 结果集
     */
    Map<String, List<Map<String, Long>>> findAssetUpdate();

    /**
     * 更新资产的风险状态
     *
     * @param assetId 资产id
     */
    void updateRiskStatus(Long assetId);

    /**
     * 资产数量 -存活 -下线等
     *
     * @return
     */
    List<Map<Object, Object>> findAssetSum();

    /**
     * 高频漏洞
     *
     * @return 结果集
     */
    List<Map<String, Object>> findHighLeak();

    /**
     * 资产变更趋势
     *
     * @return 结果集
     */
    Map<String, List<Map<String, Long>>> findAssetUpdateTrend();

    /**
     * 实时告警
     *
     * @return 结果集
     */
    List<Map<Object, Object>> findRealtimeAlarm();

    /**
     * 资产风险概况
     *
     * @return 结果集
     */
    List<Map<String, Object>> findRisk();

    /**
     * 主机数-端口数-网站数
     *
     * @return 结果集
     */
    List<Map<String, Object>> findAssetNum();

    /**
     * 部门
     *
     * @return 结果集
     */
    List<Map<String, Object>> findDeptNum();

    /**
     * 地图统计
     *
     * @return 结果集
     */
    List<Map<String, Object>> findMap();

    /**
     * 风险趋势
     *
     * @return 结果集
     */
    List<Map<String, Object>> findRiskTrend();

    /**
     * 更新Asset 的标签
     *
     * @param assetId 资产id
     * @param assetTagList 资产标签列表
     */
    void updateAssetTag(Long assetId, AssetTag... assetTagList);
}
