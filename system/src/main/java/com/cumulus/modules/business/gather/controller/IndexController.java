package com.cumulus.modules.business.gather.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * es 索引
 *
 * @author Shijh
 */
@RequestMapping("/index/")
@PreAuthorize("@auth.check('assetWarehouse')")
@RestController
public class IndexController {
    /**
     * es模板
     */
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @return 结果
     */
    @GetMapping("delete")
    public String delete(@RequestParam String indexName) {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(IndexCoordinates.of(indexName));
        indexOperations.delete();
        return "索引删除成功";
    }
}
