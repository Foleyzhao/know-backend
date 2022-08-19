package com.cumulus.modules.system.service.impl;

import com.cumulus.modules.system.dto.DictDetailDto;
import com.cumulus.modules.system.dto.DictDetailQueryCriteria;
import com.cumulus.modules.system.entity.Dict;
import com.cumulus.modules.system.entity.DictDetail;
import com.cumulus.modules.system.mapstruct.DictDetailMapper;
import com.cumulus.modules.system.repository.DictDetailRepository;
import com.cumulus.modules.system.repository.DictRepository;
import com.cumulus.modules.system.service.DictDetailService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统字典详情服务实现
 */
@Service
@CacheConfig(cacheNames = "dict")
public class DictDetailServiceImpl implements DictDetailService {

    /**
     * 系统字典数据访问接口
     */
    @Autowired
    private DictRepository dictRepository;

    /**
     * 系统字典详情数据访问接口
     */
    @Autowired
    private DictDetailRepository dictDetailRepository;

    /**
     * 系统字典详情传输对象与系统字典详情实体的映射
     */
    @Autowired
    private DictDetailMapper dictDetailMapper;

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Map<String, Object> queryAll(DictDetailQueryCriteria criteria, Pageable pageable) {
        Page<DictDetail> page = dictDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtils.toPage(page.map(dictDetailMapper::toDto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictDetail resources) {
        dictDetailRepository.save(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetail resources) {
        DictDetail dictDetail = dictDetailRepository.findById(resources.getId()).orElseGet(DictDetail::new);
        ValidationUtils.isNull(dictDetail.getId(), "DictDetail", "id", resources.getId());
        resources.setId(dictDetail.getId());
        dictDetailRepository.save(resources);
        // 清理缓存
        delCaches(resources);
    }

    @Override
    @Cacheable(key = "'name:' + #p0")
    public List<DictDetailDto> getDictByName(String name) {
        return dictDetailMapper.toDto(dictDetailRepository.findByDictName(name));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        DictDetail dictDetail = dictDetailRepository.findById(id).orElseGet(DictDetail::new);
        // 清理缓存
        delCaches(dictDetail);
        dictDetailRepository.deleteById(id);
    }

    /**
     * 删除字典缓存
     *
     * @param dictDetail 字典详情
     */
    public void delCaches(DictDetail dictDetail) {
        Dict dict = dictRepository.findById(dictDetail.getDict().getId()).orElseGet(Dict::new);
        redisUtils.del(CacheKey.DICT_NAME + dict.getName());
    }

}
