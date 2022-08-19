package com.cumulus.modules.business.repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cumulus.modules.business.dto.AssetQueryCriteria;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.system.entity.Dept;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产数据访问接口
 *
 * @author zhangxq
 */
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {


    /**
     * 根据父资产查询
     *
     * @param parent   父资产
     * @param pageable 分页参数
     * @return 子资产列表
     */
    Page<Asset> findAllByParentEquals(Asset parent, Pageable pageable);

    /**
     * 查询相关主机资产及应用资产
     *
     * @param completeIp 全写ip
     * @return 主机资产
     */
    List<Asset> queryAssetsByCompleteIpEquals(String completeIp);

    /**
     * 查询子资产数
     *
     * @param id 父id
     * @return 子资产数
     */
    Long countByParentIdEquals(Long id);

    /**
     * 查询端口是否重复
     *
     * @param completeIp 全写ip
     * @param port       端口
     * @return 资产
     */
    Asset queryAssetByCompleteIpEqualsAndPortEqualsAndAssetCategoryEquals(
            String completeIp, Integer port, Integer category);

    /**
     * 查询主机资产是否重复
     *
     * @param completeIp    全写ip
     * @param assetCategory 资产类别 1主机 2应用
     * @return 资产
     */
    List<Asset> queryAssetByCompleteIpEqualsAndAssetCategoryEquals(String completeIp, Integer assetCategory);

    /**
     * 更新资产的采集状态
     *
     * @param id         资产id
     * @param assetState 资产状态
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE tbl_asset SET asset_status = ?2 where id = ?1", nativeQuery = true)
    void updateAssetStatus(Long id, Integer assetState);

    /**
     * 根据部门获取资产列表
     *
     * @param dept 部门列表
     * @return 返回资产列表
     */
    List<Asset> findAllByDeptIn(Collection<Dept> dept);

    /**
     * 统计当前系统类型资产数
     *
     * @param assetSysTypes 子类型
     * @return 数量
     */
    @Query(value = "select count(*) from tbl_asset where asset_sys_type_id in ?1", nativeQuery = true)
    Integer countAllByAssetSysTypeIsIn(Set<Integer> assetSysTypes);

    /**
     * 统计当前系统类型资产数
     *
     * @param assetTypes 父类型
     * @return 数量
     */
    @Query(value = "select count(1) from tbl_asset where asset_type_id in ?1", nativeQuery = true)
    Integer countAllByAssetTypeIsIn(Set<Integer> assetTypes);

    /**
     * 复杂分页查询列表 父资产
     *
     * @param asset    查询条件
     * @param pageable 分页条件 pageable会自动拼接到sql后面
     * @return 对象列表
     */
    @Query(value =
            "select ta.* from tbl_asset ta RIGHT JOIN " +
                    "(" +
                    "select asset.ip ip, MAX(tag.tag_id) tag_id from tbl_asset asset " +
                    "LEFT JOIN tbl_asset_tag_mapping tag on asset.id = tag.asset_id " +
                    "LEFT JOIN tbl_asset_extend extend on asset.extend_id = extend.id " +
                    "where 1 = 1 " +
                    "and if(:#{#asset.blurry} != '', " +
                    "(asset.ip  LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.name LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.port LIKE CONCAT('%',:#{#asset.blurry},'%')), " +
                    "1 = 1)" +
                    "and if(:#{#asset.assetTypeIds.size()} != 0, asset.asset_type_id  in :#{#asset.assetTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetSysTypeIds.size()} != 0, asset.asset_sys_type_id  in :#{#asset.assetSysTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetTagIds.size()} != 0, tag.tag_id  in :#{#asset.assetTagIds}, 1 = 1)" +
                    "and if(:#{#asset.ip} != '', asset.ip  LIKE CONCAT('%',:#{#asset.ip},'%'), 1 = 1)" +
                    "and if(:#{#asset.port} != '', asset.port  LIKE CONCAT('%',:#{#asset.port},'%'), 1 = 1)" +
                    "and if(:#{#asset.name} != '', asset.name  LIKE CONCAT('%',:#{#asset.name},'%'), 1 = 1)" +
                    "and if(:#{#asset.deptIds.size()} != 0, asset.dept_id  in :#{#asset.deptIds}, 1 = 1)" +
                    "and if(:#{#asset.riskLevels.size()} != 0, asset.risk_level  in :#{#asset.riskLevels}, 1 = 1)" +
                    "and if(:#{#asset.assetStatus.size()} != 0, asset.asset_status  in :#{#asset.assetStatus}, 1 = 1)" +
                    "and if(:#{#asset.webAddress} != '', extend.website  LIKE CONCAT('%',:#{#asset.webAddress},'%'), 1 = 1)" +
                    "and if(:#{#asset.fingerprint} != '', asset.fingerprint  LIKE CONCAT('%',:#{#asset.fingerprint},'%'), 1 = 1)" +
                    "and if(:#{#asset.minUpdateTime} != '', asset.update_time > :#{#asset.minUpdateTime}, 1 = 1)" +
                    "and if(:#{#asset.maxUpdateTime} != '', asset.update_time < :#{#asset.maxUpdateTime}, 1 = 1)" +
                    "GROUP BY asset.ip) ip " +
                    "on ta.ip = ip.ip where ta.asset_category  = 1"
            , nativeQuery = true)
    List<Asset> findList(AssetQueryCriteria asset, Pageable pageable);

    /**
     * 复杂查询列表 父资产总记录数
     *
     * @param asset 查询条件
     * @return 总记录树
     */
    @Query(value =
            "select count(*) from tbl_asset ta RIGHT JOIN " +
                    "(" +
                    "select asset.ip ip, MAX(tag.tag_id) tag_id from tbl_asset asset " +
                    "LEFT JOIN tbl_asset_tag_mapping tag on asset.id = tag.asset_id " +
                    "LEFT JOIN tbl_asset_extend extend on asset.extend_id = extend.id " +
                    "where 1 = 1 " +
                    "and if(:#{#asset.blurry} != '', " +
                    "(asset.ip  LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.name LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.port LIKE CONCAT('%',:#{#asset.blurry},'%')), " +
                    "1 = 1)" +
                    "and if(:#{#asset.assetTypeIds.size()} != 0, asset.asset_type_id  in :#{#asset.assetTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetSysTypeIds.size()} != 0, asset.asset_sys_type_id  in :#{#asset.assetSysTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetTagIds.size()} != 0, tag.tag_id  in :#{#asset.assetTagIds}, 1 = 1)" +
                    "and if(:#{#asset.ip} != '', asset.ip  LIKE CONCAT('%',:#{#asset.ip},'%'), 1 = 1)" +
                    "and if(:#{#asset.port} != '', asset.port  LIKE CONCAT('%',:#{#asset.port},'%'), 1 = 1)" +
                    "and if(:#{#asset.name} != '', asset.name  LIKE CONCAT('%',:#{#asset.name},'%'), 1 = 1)" +
                    "and if(:#{#asset.deptIds.size()} != 0, asset.dept_id  in :#{#asset.deptIds}, 1 = 1)" +
                    "and if(:#{#asset.riskLevels.size()} != 0, asset.risk_level  in :#{#asset.riskLevels}, 1 = 1)" +
                    "and if(:#{#asset.assetStatus.size()} != 0, asset.asset_status  in :#{#asset.assetStatus}, 1 = 1)" +
                    "and if(:#{#asset.webAddress} != '', extend.website  LIKE CONCAT('%',:#{#asset.webAddress},'%'), 1 = 1)" +
                    "and if(:#{#asset.fingerprint} != '', asset.fingerprint  LIKE CONCAT('%',:#{#asset.fingerprint},'%'), 1 = 1)" +
                    "and if(:#{#asset.minUpdateTime} != '', asset.update_time > :#{#asset.minUpdateTime}, 1 = 1)" +
                    "and if(:#{#asset.maxUpdateTime} != '', asset.update_time < :#{#asset.maxUpdateTime}, 1 = 1)" +
                    "GROUP BY asset.ip) ip " +
                    "on ta.ip = ip.ip where ta.asset_category  = 1"
            , nativeQuery = true)
    Integer findListCount(AssetQueryCriteria asset);

    /**
     * 复杂查询列表 父资产总记录数
     *
     * @param asset 查询条件
     * @return 总记录树
     */
    @Query(value =
            "SELECT max(asset.risk_level) risk_level FROM `tbl_asset` asset " +
                    "LEFT JOIN tbl_asset_tag_mapping tag on asset.id = tag.asset_id " +
                    "LEFT JOIN tbl_asset_extend extend on asset.extend_id = extend.id " +
                    "where asset.asset_category = 2 " +
                    "and if(:#{#asset.blurry} != '', " +
                    "(asset.ip  LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.name LIKE CONCAT('%',:#{#asset.blurry},'%') " +
                    "or asset.port LIKE CONCAT('%',:#{#asset.blurry},'%')), " +
                    "1 = 1)" +
                    "and if(:#{#asset.assetTypeIds.size()} != 0, asset.asset_type_id  in :#{#asset.assetTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetSysTypeIds.size()} != 0, asset.asset_sys_type_id  in :#{#asset.assetSysTypeIds}, 1 = 1)" +
                    "and if(:#{#asset.assetTagIds.size()} != 0, tag.tag_id  in :#{#asset.assetTagIds}, 1 = 1)" +
                    "and if(:#{#asset.ip} != '', asset.ip  LIKE CONCAT('%',:#{#asset.ip},'%'), 1 = 1)" +
                    "and if(:#{#asset.port} != '', asset.port  LIKE CONCAT('%',:#{#asset.port},'%'), 1 = 1)" +
                    "and if(:#{#asset.name} != '', asset.name  LIKE CONCAT('%',:#{#asset.name},'%'), 1 = 1)" +
                    "and if(:#{#asset.deptIds.size()} != 0, asset.dept_id  in :#{#asset.deptIds}, 1 = 1)" +
                    "and if(:#{#asset.riskLevels.size()} != 0, asset.risk_level  in :#{#asset.riskLevels}, 1 = 1)" +
                    "and if(:#{#asset.assetStatus.size()} != 0, asset.asset_status  in :#{#asset.assetStatus}, 1 = 1)" +
                    "and if(:#{#asset.webAddress} != '', extend.website  LIKE CONCAT('%',:#{#asset.webAddress},'%'), 1 = 1)" +
                    "and if(:#{#asset.fingerprint} != '', asset.fingerprint  LIKE CONCAT('%',:#{#asset.fingerprint},'%'), 1 = 1)" +
                    "and if(:#{#asset.minUpdateTime} != '', asset.update_time > :#{#asset.minUpdateTime}, 1 = 1)" +
                    "and if(:#{#asset.maxUpdateTime} != '', asset.update_time < :#{#asset.maxUpdateTime}, 1 = 1)"
            , nativeQuery = true)
    Integer findChildMaxRisk(AssetQueryCriteria asset);

    /**
     * 根据风险状态资产仓库的计数信息整体信息
     *
     * @param riskLevel 风险状态列表
     * @return 返回计数
     */
    Long countAllByRiskLevelIn(Collection<Integer> riskLevel);

    /**
     * 根据资产状态查询资产仓库的计数信息整体信息
     *
     * @param assetStatus 资产状态列表
     * @return 返回计数
     */
    Long countAllByAssetStatusIn(Collection<Integer> assetStatus);

    /**
     * 根据id更新某个资产的采集ID
     *
     * @param id            资产Id
     * @param gatherAssetId 资产采集Id
     */
    @Modifying
    @Query(value = "UPDATE tbl_asset SET gather_asset_id= ?2 where id = ?1", nativeQuery = true)
    void updateGatherAssetIdById(Long id, String gatherAssetId);

    /**
     * 根据id更新某个资产的扫描ID
     *
     * @param id            资产Id
     * @param scanAssetId 远程扫描Id
     */
    @Modifying
    @Query(value = "UPDATE tbl_asset SET scan_asset_id= ?2 where id = ?1", nativeQuery = true)
    void updateScanAssetIdById(Long id, String scanAssetId);

    /**
     * 根据id更新登录状态
     *
     * @param id          id
     * @param loginStatus 登录测试状态
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE tbl_asset SET login_status= ?2 where id = ?1", nativeQuery = true)
    void updateLoginStatusById(Long id, Integer loginStatus);

    /**
     * 查询当前资产类型的资产
     *
     * @param assetType 父资产类型
     * @return 资产列表
     */
    List<Asset> findAllByAssetTypeEquals(AssetType assetType);

    /**
     * 查询当前资产类型的资产
     *
     * @param assetSysType 子资产类型
     * @return 资产列表
     */
    List<Asset> findAllByAssetSysTypeEquals(AssetSysType assetSysType);

    /**
     * 根据部门获取资产计数
     *
     * @param dept 部门列表
     * @return 返回计数
     */
    Integer countAllByDeptIn(Collection<Dept> dept);

    /**
     * 根据父资产查询
     *
     * @param assets
     * @return
     */
    Long countByParentIn(Collection<Asset> assets);

    /**
     * 根据资产类别计数
     *
     * @param category
     * @return
     */
    Long countAllByAssetCategoryEquals(Integer category);

    /**
     * 根据ip获取资产列表
     *
     * @param ip ip
     * @return 返回列表
     */
    List<Asset> findAllByIp(String ip);

    /**
     * 根据ip获取资产列表
     *
     * @param ip            ip
     * @param assetCategory 资产类型
     * @return 返回列表
     */
    List<Asset> findAllByIpAndAssetCategory(String ip, Integer assetCategory);

    /**
     * 根据ip获取资产列表
     *
     * @param website 网址
     * @return 返回列表
     */
    @Query(nativeQuery = true, value = "SELECT * FROM tbl_asset ta INNER JOIN tbl_asset_extend tae ON ta.extend_id = tae.id WHERE tae.website = ?1")
    List<Asset> findAllByWebSite(String website);

    /**
     * 根据ip获取资产列表
     *
     * @param ip            ip
     * @param assetCategory 资产类别
     * @param port          资产端口
     * @return 返回列表
     */
    List<Asset> findAllByIpAndAssetCategoryAndPort(String ip, Integer assetCategory, Integer port);

    /**
     * 根据全写ip删除
     *
     * @param CompleteIp
     */
    @Transactional
    @Modifying
    void deleteByCompleteIpEquals(String CompleteIp);

    /**
     * 根据名称查重
     *
     * @param name 资产名称
     * @return 个数
     */
    Long countByNameEquals(String name);

    /**
     * 综控台-数据分析
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select (" +
            "select count(1) from tbl_asset) as '总数'," +
            "(select count(DISTINCT complete_ip) from tbl_asset where asset_category=1 ) as '主机'," +
            "(select count(DISTINCT complete_ip,port) from tbl_asset where asset_category=2 ) as '应用'," +
            "(select count(1) from tbl_asset where asset_status=0) as '存活',\n" +
            "(select count(1) from tbl_asset where asset_status=1) as '下线'" +
            "from tbl_asset limit 1")
    List<Map<String, String>> findDataAnalysis();

    /**
     * 综控台-部门分布
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select\n" +
            "d.name name,\n" +
            "count(a.dept_id) num\n" +
            "from \n" +
            "tbl_asset a\n" +
            "LEFT join sys_dept d on a.dept_id=d.id\n" +
            "GROUP BY a.dept_id HAVING d.name is not null\n" +
            "ORDER BY num desc ")
    List<Map<String, String>> findDept();

    /**
     * 综控台-应用资产-网络服务
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select \n" +
            "e.name name,\n" +
            "count(e.name) num\n" +
            "from tbl_asset a\n" +
            "LEFT join tbl_asset_extend e \n" +
            "on a.extend_id=e.id\n" +
            "where a.asset_category=2\n" +
            "GROUP BY e.name HAVING e.name is not null\n" +
            "ORDER BY count(e.name) Desc ")
    List<Map<String, String>> findApplyAsset();

    /**
     * 综控台-应用资产-程序框架
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select \n" +
            "e.product name,\n" +
            "count(e.product) num\n" +
            "from tbl_asset a\n" +
            "LEFT join tbl_asset_extend e \n" +
            "on a.extend_id=e.id\n" +
            "where a.asset_category=2\n" +
            "GROUP BY e.product HAVING e.product is not null\n" +
            "ORDER BY count(e.product) Desc \n")
    List<Map<String, String>> findProcedure();

    /**
     * 综控台-主机资产信息-主机端口
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "SELECT c.name,c.num FROM (select port as name,\n" +
            "count(port) as num\n" +
            "from tbl_asset \n" +
            "group by port HAVING port is not null\n" +
            "ORDER BY count(port) Desc limit 4) c\n" +
            "UNION\n" +
            "SELECT \n" +
            "'其他',COUNT(a.port) \n" +
            "FROM (\n" +
            "SELECT s.* FROM tbl_asset s WHERE  s.port not in (\n" +
            "SELECT t.port FROM (\n" +
            "SELECT port , COUNT(port) as num FROM tbl_asset  GROUP BY port ORDER BY num DESC limit 4)\n" +
            "t)) a")
    List<Map<String, Object>> findHost();

    /**
     * 综控台-主机资产信息-主机服务
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "SELECT c.name,c.num FROM (select protocol as name,\n" +
            "count(protocol) as num\n" +
            "from tbl_asset where asset_category=1\n" +
            "group by protocol HAVING protocol is not null \n" +
            "ORDER BY count(protocol) Desc limit 4) c\n" +
            "UNION\n" +
            "SELECT \n" +
            "'其他',COUNT(a.protocol) \n" +
            "FROM (\n" +
            "SELECT s.* FROM tbl_asset s WHERE s.asset_category=1 and s.protocol not in (\n" +
            "SELECT t.protocol FROM (\n" +
            "SELECT protocol , COUNT(protocol) as num FROM tbl_asset  where asset_category=1 GROUP BY protocol ORDER BY num DESC limit 4) \n" +
            "t)) a ")
    List<Map<String, Object>> findHostServer();

    /**
     * 综控台-主机资产信息-操作系统
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select t.name name,\n" +
            "count(*) num\n" +
            "from tbl_asset a\n" +
            "LEFT join tbl_asset_sys_type t\n" +
            "on a.asset_sys_type_id=t.id\n" +
            "where a.asset_category=1 and a.asset_type_id=1 and t.name is not null \n" +
            "group by a.asset_sys_type_id \n" +
            "ORDER BY count(a.asset_sys_type_id) Desc limit 0,4")
    List<Map<String, Object>> findSystem();

    /**
     * 统计全部操作系统
     *
     * @return
     */
    @Query(nativeQuery = true, value = "select count(*) from tbl_asset where asset_category=1 and asset_type_id=1")
    int countAllSystem();

    /**
     * 综控台-资产概况-类型分布
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select t.name name,\n" +
            "count(a.asset_type_id) num\n" +
            "from tbl_asset a\n" +
            "left join tbl_asset_type t\n" +
            "on a.asset_type_id=t.id\n" +
            "group by a.asset_type_id HAVING t.name is not null \n" +
            "ORDER BY count(a.asset_sys_type_id) Desc  limit 0,8 ")
    List<Map<String, String>> findAssetType();


    /**
     * 综控台-资产概况-标签分布
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select tat.name name, count(*) num from " +
            "tbl_asset_tag tat\n" +
            "INNER JOIN tbl_asset_tag_mapping tatm on tat.id=tatm.tag_id \n" +
            "GROUP BY tatm.tag_id ORDER BY count(*) desc  limit 0,8;")
    List<Map<String, String>> findAssetTag();

    /**
     * 综控台-风险统计-风险概况
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select \n" +
            "           sum(risk_level=0) '安全',\n" +
            "           sum(risk_level=1) '低危',\n" +
            "           sum(risk_level=2) '中危',\n" +
            "           sum(risk_level=3) '高危'\n" +
            "            from tbl_asset ")
    List<Map<String, Object>> countRisk();

    /**
     * 综控台-风险统计-风险趋势
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select\n" +
            "a.click_date,\n" +
            " ifnull(b.n,0) '低危',\n" +
            "  ifnull(b.nn,0) '中危',\n" +
            "\t ifnull(b.nnn,0) '高危'\n" +
            "from (\n" +
            "SELECT date_sub(curdate(), interval 1 day) as click_date\n" +
            "union all\n" +
            "SELECT date_sub(curdate(), interval 2 day) as click_date\n" +
            "union all\n" +
            "SELECT date_sub(curdate(), interval 3 day) as click_date\n" +
            "union all\n" +
            "SELECT date_sub(curdate(), interval 4 day) as click_date\n" +
            "union all\n" +
            "SELECT date_sub(curdate(), interval 5 day) as click_date\n" +
            "union all \n" +
            "SELECT date_sub(curdate(), interval 6 day) as click_date\n" +
            ") a left join (\n" +
            "select date(DATE_FORMAT(create_time,'%Y-%m-%d')) as datetime,\n" +
            "low_risk_num n,\n" +
            "in_danger_num nn,\n" +
            "high_num nnn\n" +
            "from count_vulnerability\n" +
            "group by datetime\n" +
            ") b \n" +
            "on a.click_date = b.datetime order by a.click_date desc;;")
    List<Map<String, Object>> countRiskDetail();

    /**
     * 根据任务id查询资产列表
     *
     * @param id       id
     * @param pageable 分页
     * @return 资产列表
     */
    @Query(value = "select * from tbl_asset where id in (select asset_id from tbl_plan_asset where plan_id = ?1 )",
            nativeQuery = true)
    Page<Asset> findAssetsById(Long id, Pageable pageable);

    /**
     * 查询资产总数 -存活 -下线
     *
     * @return 结果集
     */
    @Query(value = "select\n" +
            "            count(1) 'assetSum',\n" +
            "            sum(asset_status=0) 'survive',\n" +
            "            IFNULL(sum(asset_status=1),0) 'offlive' \n" +
            "            from tbl_asset",nativeQuery = true)
    List<Map<Object, Object>> findAssetSum();

    /**
     * 查询主机数-端口数-网站数
     *
     * @return 结果集
     */
    @Query(value = "select \n" +
            "sum(t.asset_category=1) 'hostNum',\n" +
            "sum(t.port is not null) 'portNum',\n" +
            "sum(tt.website is not null) 'webNum'\n" +
            "from tbl_asset t\n" +
            "LEFT JOIN tbl_asset_extend tt\n" +
            "on t.extend_id=tt.id \n",nativeQuery = true)
    List<Map<String, Object>> findAssetNum();

    /**
     * 查询资产总数 -存活 -下线 增量数据
     *
     * @return 结果集
     */
    @Query(value = "select \n" +
            "IFNULL(sum(t.asset_type_id=1),0) 'growthHost',\n" +
            "IFNULL(sum(t.port is not null),0) 'growthPort',\n" +
            "IFNULL(sum(tt.website is not null),0) 'growthWeb'\n" +
            "from tbl_asset t\n" +
            "LEFT JOIN tbl_asset_extend tt\n" +
            "on t.extend_id = tt.id\n" +
            "where to_days(t.create_time) = to_days(now())\n",nativeQuery = true)
    List<Map<String, Object>> findGrowth();

    /**
     * 部门数量
     *
     * @return 结果集
     */
    @Query(value = "select \n" +
            "s.name 'deptName',\n" +
            "COUNT(1) 'sum',\n" +
            "sum( t.risk_level=1)'riskLow',\n" +
            "sum( t.risk_level=2) 'riskMiddle',\n" +
            "sum( t.risk_level=3) 'riskHigh',\n" +
            "sum( t.risk_level=0) 'riskSafety'\n" +
            "from tbl_asset t\n" +
            "LEFT JOIN sys_dept s on t.dept_id=s.id\n" +
            "where t.dept_id is not null \n" +
            "GROUP BY t.dept_id;\n",nativeQuery = true)
    List<Map<Object, Object>> findDeptNum();

    /**
     * 主机服务Top5
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select protocol name,\n" +
            "count(protocol) num\n" +
            "from tbl_asset where asset_category=1\n" +
            "group by protocol HAVING protocol is not null \n" +
            "ORDER BY count(protocol) Desc limit 0,5")
    List<Map<String, Object>> findServer();

    /**
     * 操作系统Top5
     *
     * @return 结果集
     */
    @Query(nativeQuery = true, value = "select t.name name,\n" +
            "count(a.asset_sys_type_id) num\n" +
            "from tbl_asset a\n" +
            "LEFT join tbl_asset_sys_type t\n" +
            "on a.asset_sys_type_id=t.id\n" +
            "where a.asset_category=1 and a.asset_type_id=1\n" +
            "group by a.asset_sys_type_id HAVING t.name is not null \n" +
            "ORDER BY count(a.asset_sys_type_id) Desc limit 0,5")
    List<Map<String, Object>> findSys();

    /**
     * 获取全部资产计数根据资产类型
     *
     * @param assetCategory 资产类型
     * @return 返回数量
     */
    Long countAllByAssetCategory(Integer assetCategory);


    /**
     * 获取全部资产计数根据资产类型
     *
     * @param assetCategory 资产类型
     * @param dept          部门列表
     * @return 返回数量
     */
    Long countAllByAssetCategoryAndDeptIn(Integer assetCategory, Collection<Dept> dept);

    /**
     * 根据部门id查询资产数量
     *
     * @param set 部门id
     * @return 结果集
     */
    @Query(value = "select count(*) from tbl_asset where dept_id in ?1",nativeQuery = true)
    int countNum(Set<String> set);

    /**
     * 地图统计
     *
     * @return 结果集
     */
    @Query(value = "select t.ip 'ip',\n" +
            "t.risk_level 'level',\n" +
            "tt.lat 'lat',\n" +
            "tt.lon  'lon'\n" +
            "from tbl_asset t\n" +
            "LEFT JOIN tbl_asset_extend tt on t.extend_id = tt.id where asset_category=1 LIMIT 570",nativeQuery = true)
    List<Map<String, Object>> findMap();
}
