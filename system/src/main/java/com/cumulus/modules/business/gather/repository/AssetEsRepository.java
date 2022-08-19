package com.cumulus.modules.business.gather.repository;


import java.util.List;

import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 资产数据访问接口
 *
 * @author Shijh
 */
public interface AssetEsRepository extends ElasticsearchRepository<GatherAssetEs, String> {

    /**
     * 查询子或者父
     *
     * @param ip   ip
     * @param diff 0-主机 1-应用
     * @return
     */
    List<GatherAssetEs> queryByIpAndDiff(String ip, Integer diff);

}
