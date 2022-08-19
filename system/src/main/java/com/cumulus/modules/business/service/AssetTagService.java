package com.cumulus.modules.business.service;

import java.util.List;
import java.util.Set;

import com.cumulus.modules.business.dto.AssetTagListTreeDto;
import com.cumulus.modules.business.dto.AssetTagTreeDto;
import com.cumulus.modules.business.entity.AssetTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产标签服务接口
 *
 * @author zhangxq
 */
public interface AssetTagService {

    /**
     * 批量新增
     *
     * @param file 标签列表文件
     * @return 标签列表
     */
    Object createBatch(MultipartFile file);

    /**
     * 添加标签
     *
     * @param assetTag 标签传输对象
     */
    void create(AssetTag assetTag);

    /**
     * 分页查询资产标签
     *
     * @param pageable 分页参数
     * @return 资产标签列表
     */
    Object queryAll(Pageable pageable);

    /**
     * 分页查询资产标签树形
     *
     * @param pageable 分页参数
     * @return 返回分页数据
     */
    Page<AssetTagTreeDto> queryTreePage(Pageable pageable);

    /**
     * 分页查询资产标签树形
     *
     * @param parentTagId 标签id
     * @param pageable    分页参数
     * @return 返回分页数据
     */
    Page<AssetTag> queryTreePageSub(Long parentTagId, Pageable pageable);

    /**
     * 标签下拉框
     *
     * @return 资产标签列表
     */
    List<AssetTagListTreeDto> querySelect();

    /**
     * 根据id删除
     *
     * @param tagId 标签id
     */
    void removeById(Long tagId);

    /**
     * 批量删除
     *
     * @param ids    id列表
     * @param delAll 是否全部删除
     * @return 删除结果
     */
    String removeBatch(Set<Long> ids, Boolean delAll);

    /**
     * 修改资产标签
     *
     * @param assetTag 资产标签传输对象
     */
    void updateById(AssetTag assetTag);
}
