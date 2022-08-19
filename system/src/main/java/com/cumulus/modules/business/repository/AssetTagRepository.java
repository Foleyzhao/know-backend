package com.cumulus.modules.business.repository;

import com.cumulus.modules.business.entity.AssetTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 资产标签数据访问接口
 */
public interface AssetTagRepository extends JpaRepository<AssetTag, Long>, JpaSpecificationExecutor<AssetTag> {

    /**
     * 根据标签名查询
     *
     * @param name 标签名
     * @return 资产标签对象
     */
    AssetTag findAssetTagByNameEquals(String name);

    /**
     * 根据标签名查询
     *
     * @param name 标签名
     * @return 资产标签对象
     */
    Optional<AssetTag> findByName(String name);

    /**
     * 根据标签编号查询
     *
     * @param number 编号
     * @return 资产标签对象
     */
    Optional<AssetTag> findByNumber(String number);

    /**
     * 查询全部非定制的标签
     *
     * @return 返回列表
     */
    List<AssetTag> findAllByCustomizeFalse();

    /**
     * 根据标签编号查询
     *
     * @param parent 父类
     * @return 资产标签对象
     */
    Long countAllByParent(AssetTag parent);

    /**
     * 获取父类标签
     *
     * @param pageable 分页参数
     * @return 父类标签分页
     */
    Page<AssetTag> findAllByParentIsNullAndEnabledIsTrue(Pageable pageable);

    /**
     * 获取全部标签
     *
     * @return 父类标签分页
     */
    List<AssetTag> findAllByParentIsNullAndEnabledIsTrue();

    /**
     * 获取子类标签
     *
     * @param parent   父类资产
     * @param pageable 分页参数
     * @return 父类标签分页
     */
    Page<AssetTag> findAllByParentAndEnabledIsTrue(AssetTag parent, Pageable pageable);

    /**
     * 获取父类标签
     *
     * @param parent   父类资产
     * @return 父类标签分页
     */
    List<AssetTag> findAllByParentAndEnabledIsTrue(AssetTag parent);

    /**
     * 查询是否被使用
     *
     * @return 数量
     */
    @Query(value = "select count(1) from tbl_asset_tag_mapping m " +
            "left join tbl_asset_tag t on m.tag_id = t.id " +
            "where t.customize = false",
            nativeQuery = true)
    Integer countTagUsed();

    /**
     * 按内置条件删除
     *
     * @param customize 内置
     */
    void deleteByCustomizeEquals(boolean customize);

    /**
     * 根据资产id获取资产标签列表
     *
     * @param assetId 资产Id
     * @return 返回列表
     */
    @Query(value = "select * from tbl_asset_tag tag right join tbl_asset_tag_mapping map on tag.id = map.tag_id " +
            "where map.asset_id = ?1",
            nativeQuery = true)
    List<AssetTag> findAllByAssetId(Long assetId);

    /**
     * 根据父类查询子标签
     *
     * @param parentId 父类id
     * @return 返回列表
     */
    @Query(value = "select * from tbl_asset_tag where parent_id = ?1 and ?2 like CONCAT(name,'%')  LIMIT 1",
            nativeQuery = true)
    AssetTag findByParentIs(Long parentId,String middlewareName);

}
