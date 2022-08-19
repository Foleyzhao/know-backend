package com.cumulus.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cumulus.modules.system.dto.DictDetailDto;
import com.cumulus.modules.system.dto.DictDto;
import com.cumulus.modules.system.dto.DictQueryCriteria;
import com.cumulus.modules.system.entity.Dict;
import com.cumulus.modules.system.mapstruct.DictMapper;
import com.cumulus.modules.system.repository.DictRepository;
import com.cumulus.modules.system.service.DictService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典服务实现
 */
@Service
@CacheConfig(cacheNames = "dict")
public class DictServiceImpl implements DictService {

    /**
     * 字典数据访问接口
     */
    @Autowired
    private DictRepository dictRepository;

    /**
     * 系统字典传输对象与系统字典实体的映射
     */
    @Autowired
    private DictMapper dictMapper;

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public Map<String, Object> queryAll(DictQueryCriteria dict, Pageable pageable) {
        Page<Dict> page = dictRepository.findAll((root, query, cb) -> QueryUtils.getPredicate(root, dict, cb), pageable);
        return PageUtils.toPage(page.map(dictMapper::toDto));
    }

    @Override
    public List<DictDto> queryAll(DictQueryCriteria dict) {
        List<Dict> list = dictRepository.findAll((root, query, cb) -> QueryUtils.getPredicate(root, dict, cb));
        return dictMapper.toDto(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dict resources) {
        dictRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict resources) {
        // 清理缓存
        delCaches(resources);
        Dict dict = dictRepository.findById(resources.getId()).orElseGet(Dict::new);
        ValidationUtils.isNull(dict.getId(), "Dict", "id", resources.getId());
        dict.setName(resources.getName());
        dict.setDescription(resources.getDescription());
        dictRepository.save(dict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        // 清理缓存
        List<Dict> dicts = dictRepository.findByIdIn(ids);
        for (Dict dict : dicts) {
            delCaches(dict);
        }
        dictRepository.deleteByIdIn(ids);
    }

    @Override
    public void download(List<DictDto> dictDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictDto dictDTO : dictDtos) {
            if (CollectionUtil.isNotEmpty(dictDTO.getDictDetails())) {
                for (DictDetailDto dictDetail : dictDTO.getDictDetails()) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("字典名称", dictDTO.getName());
                    map.put("字典描述", dictDTO.getDescription());
                    map.put("字典标签", dictDetail.getLabel());
                    map.put("字典值", dictDetail.getValue());
                    map.put("创建日期", dictDetail.getCreateTime());
                    list.add(map);
                }
            } else {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("字典名称", dictDTO.getName());
                map.put("字典描述", dictDTO.getDescription());
                map.put("字典标签", null);
                map.put("字典值", null);
                map.put("创建日期", dictDTO.getCreateTime());
                list.add(map);
            }
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 删除字典缓存
     *
     * @param dict 字典
     */
    public void delCaches(Dict dict) {
        redisUtils.del(CacheKey.DICT_NAME + dict.getName());
    }
}
