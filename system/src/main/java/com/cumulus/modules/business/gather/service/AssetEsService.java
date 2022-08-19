package com.cumulus.modules.business.gather.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.vo.AssetsWarehouseVo;

import org.springframework.data.domain.Pageable;

/**
 * 资产接口服务
 *
 * @author Shijh
 */
public interface AssetEsService {

    /**
     * 批量添加参数
     *
     * @param assets 参数列表
     */
    void saveAll(List<GatherAssetEs> assets);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 统计资产状态,风险状态
     *
     * @return 结果集
     */
    Object countAssetStart();

    /**
     * 根据id查询
     *
     * @param id 要查询
     * @return 结果集
     */
    GatherAssetEs findById(String id);

    /**
     * 通过资产ip查询子资产
     *
     * @param ip 要查询资产ip下的端口资产
     * @param vo 条件
     * @return 结果集
     */
    Object findByPortAsset(String ip, Pageable pageable, AssetsWarehouseVo vo);

    /**
     * 根据id进行修改
     *
     * @param asset 要修改的对象
     */
    void updateById(GatherAssetEs asset);

    /**
     * 导出数据
     *
     * @param ids      资产id
     * @param response 响应
     */
    void exportData(Map<String, List<String>> ids, HttpServletResponse response,String name);

}
