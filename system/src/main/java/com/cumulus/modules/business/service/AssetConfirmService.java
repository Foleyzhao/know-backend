package com.cumulus.modules.business.service;

import java.util.Collection;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.modules.business.dto.AssetConfirmDto;
import com.cumulus.modules.business.dto.AssetConfirmQueryCriteria;
import com.cumulus.modules.business.dto.BatchPackage;
import org.springframework.data.domain.Pageable;

/**
 * 确认资产服务接口
 *
 * @author zhangxq
 */
public interface AssetConfirmService {

    /**
     * 查询确认资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    Object queryAll(AssetConfirmQueryCriteria criteria, Pageable pageable);

    /**
     * 根据id修改
     *
     * @param assetConfirmDto 确认资产传输对象
     */
    void updateById(AssetConfirmDto assetConfirmDto);

    /**
     * 根据id删除
     *
     * @param id
     */
    void removeById(Long id);

    /**
     * 批量删除
     *
     * @param ids    id
     * @param delAll 是否删除全部
     */
    void removeBatch(Set<Long> ids, boolean delAll);

    /**
     * 导出excel压缩包
     *
     * @param ids id列表
     * @param all 是否全部
     */
    void exportZip(Set<Long> ids, boolean all, HttpServletResponse response, HttpServletRequest request);

    /**
     * 导出excel
     *
     * @param ids id列表
     * @param all 是否全部
     */
    void exportExcel(Set<Long> ids, boolean all, HttpServletResponse response);

    /**
     * 根据ip查找端口资产
     *
     * @param ip 全写ip
     * @return 端口信息列表
     */
    Object getByIp(Pageable pageable, String ip);

    /**
     * 单个确认
     *
     * @param id
     */
    void singleConfirm(Long id);

    /**
     * 批量确认
     *
     * @param ids
     * @param isAll
     */
    void batchConfirm(Collection<Long> ids,boolean isAll);
}
