package com.cumulus.modules.business.gather.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cumulus.modules.business.gather.entity.es.AccountEs;
import com.cumulus.modules.business.gather.entity.es.GatherAssetEs;
import com.cumulus.modules.business.gather.repository.AccountEsRepository;
import com.cumulus.modules.business.gather.service.AccountEsService;
import com.cumulus.utils.PageUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 服务接口实现
 *
 * @author shijh
 */
@Service
public class AccountEsServiceImpl implements AccountEsService {

    /**
     * 账号信息数据访问接口
     */
    @Autowired
    private AccountEsRepository accountEsRepository;

    @Override
    public void saveAll(List<AccountEs> accountEsList) {
        this.accountEsRepository.saveAll(accountEsList);
    }

    @Override
    public void deleteById(String id) {
        this.accountEsRepository.deleteById(id);
    }

    @Override
    public void updateById(AccountEs accountEs) {
        this.accountEsRepository.save(accountEs);
    }


    @Override
    public Object findListRecent(String id, Pageable pageable) {
        //找到utime 最新的记录
        PageRequest uTime = PageRequest.of(0, 1, Sort.Direction.DESC, "utime");
        Page<AccountEs> topOne = accountEsRepository.findAllByGatherAssetId(id, uTime);
        if (topOne.getContent().isEmpty()) {
            return PageUtils.toPage(new PageImpl<GatherAssetEs>(new ArrayList<>()));
        }
        //找到时间和最新的utime相等的数据
        Date topTime = topOne.getContent().get(0).getUtime();
        return PageUtils.toPage(accountEsRepository.findAllByGatherAssetIdAndUtime(id, topTime, pageable));
    }

}
