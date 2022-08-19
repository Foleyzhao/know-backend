package com.cumulus.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import com.cumulus.modules.mnt.dto.DatabaseDto;
import com.cumulus.modules.mnt.dto.DatabaseQueryCriteria;
import com.cumulus.modules.mnt.entity.Database;
import com.cumulus.modules.mnt.mapstruct.DatabaseMapper;
import com.cumulus.modules.mnt.repository.DatabaseRepository;
import com.cumulus.modules.mnt.service.DatabaseService;
import com.cumulus.modules.mnt.util.SqlUtils;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 数据库服务实现
 */
@Slf4j
@Service
public class DatabaseServiceImpl implements DatabaseService {

    /**
     * 数据库数据访问接口
     */
    @Autowired
    private DatabaseRepository databaseRepository;

    /**
     * 数据库传输对象与数据库实体的映射
     */
    @Autowired
    private DatabaseMapper databaseMapper;

    @Override
    public Object queryAll(DatabaseQueryCriteria criteria, Pageable pageable) {
        Page<Database> page = databaseRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(databaseMapper::toDto));
    }

    @Override
    public List<DatabaseDto> queryAll(DatabaseQueryCriteria criteria) {
        return databaseMapper.toDto(databaseRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public DatabaseDto findById(String id) {
        Database database = databaseRepository.findById(id).orElseGet(Database::new);
        ValidationUtils.isNull(database.getId(), "Database", "id", id);
        return databaseMapper.toDto(database);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Database resources) {
        resources.setId(IdUtil.simpleUUID());
        databaseRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Database resources) {
        Database database = databaseRepository.findById(resources.getId()).orElseGet(Database::new);
        ValidationUtils.isNull(database.getId(), "Database", "id", resources.getId());
        database.copy(resources);
        databaseRepository.save(database);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            databaseRepository.deleteById(id);
        }
    }

    @Override
    public boolean testConnection(Database resources) {
        try {
            return SqlUtils.testConnection(resources.getJdbcUrl(), resources.getUserName(), resources.getPwd());
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            return false;
        }
    }

    @Override
    public void download(List<DatabaseDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DatabaseDto databaseDto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("数据库名称", databaseDto.getName());
            map.put("数据库连接地址", databaseDto.getJdbcUrl());
            map.put("用户名", databaseDto.getUserName());
            map.put("创建日期", databaseDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
