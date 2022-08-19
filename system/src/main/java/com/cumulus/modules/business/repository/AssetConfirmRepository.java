package com.cumulus.modules.business.repository;

import java.util.List;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.business.entity.AssetConfirm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 确认资产数据访问接口
 *
 * @author zhangxq
 */
public interface AssetConfirmRepository extends JpaRepository<AssetConfirm, Long>, JpaSpecificationExecutor<AssetConfirm> {

    /**
     * 查询资产是否重复
     *
     * @param completeIp 全写ip
     * @param port       端口
     * @return 确认资产对象
     */
    AssetConfirm queryAssetConfirmByCompleteIpEqualsAndPortEquals(String completeIp, Integer port);

    /**
     * 查询主机资产是否重复
     *
     * @param completeIp    全写ip
     * @param assetCategory 资产类别 1主机 2应用
     * @return 确认资产对象
     */
    List<AssetConfirm> queryAssetConfirmByCompleteIpEqualsAndAssetCategoryEquals(
            String completeIp, Integer assetCategory);

    /**
     * 根据资产类别查找
     *
     * @param assetCategory 资产类别
     * @return
     */
    List<AssetConfirm> queryByAssetCategoryEquals(Integer assetCategory);

    /**
     * 根据ip和类型分页查询确认资产
     *
     * @param pageable      分页参数
     * @param completeIp    ip
     * @param assetCategory 类型
     * @return 确认资产
     */
    Page<AssetConfirm> queryByCompleteIpAndAssetCategoryEquals(
            Pageable pageable, String completeIp, Integer assetCategory);

    /**
     * 根据ip、端口、资产类别删除
     *
     * @param completeIp
     * @param port
     * @param category
     */
    @Transactional
    @Modifying
    void deleteByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(String completeIp, Integer port, Integer category);

    /**
     * 根据发现任务id查找确认资产
     *
     * @param task
     * @return
     */
    List<AssetConfirm> queryAllByDetectTaskEquals(DetectTask task);

    /**
     * 删除发现任务：根据发现任务id查找确认资产 修改发现任务id为空
     *
     * @param id 发现任务id
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE tbl_asset_confirm SET detect_task_id= null where detect_task_id = ?1", nativeQuery = true)
    void updateTaskByTaskId(Long id);

    /**
     * 根据ip删除
     *
     * @param completeIp
     */
    @Transactional
    @Modifying
    void deleteByCompleteIp(String completeIp);
}
