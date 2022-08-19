package com.cumulus.modules.business.gather.service;

import java.util.List;

import com.cumulus.modules.business.gather.entity.es.AccountEs;

import org.springframework.data.domain.Pageable;

/**
 * 账号信息服务接口
 *
 * @author shijh
 */
public interface AccountEsService {

    /**
     * 批量添加参数
     *
     * @param accountEsList 参数列表
     */
    void saveAll(List<AccountEs> accountEsList);

    /**
     * 根据id 删除
     *
     * @param id id
     */
    void deleteById(String id);

    /**
     * 根据id进行修改
     *
     * @param accountEs 要修改的对象
     */
    void updateById(AccountEs accountEs);


    /**
     * 分页查询 最近的记录
     *
     * @param id       资产表的id
     * @param pageable 分页条件
     * @return 返回分页结果
     */
    Object findListRecent(String id, Pageable pageable);

}
