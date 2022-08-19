package com.cumulus.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.cumulus.config.FileProperties;
import com.cumulus.dto.LocalStorageDto;
import com.cumulus.dto.LocalStorageQueryCriteria;
import com.cumulus.entity.LocalStorage;
import com.cumulus.exception.BadRequestException;
import com.cumulus.mapstruct.LocalStorageMapper;
import com.cumulus.repository.LocalStorageRepository;
import com.cumulus.service.LocalStorageService;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地存储服务实现
 *
 * @author zhaoff
 */
@Service
public class LocalStorageServiceImpl implements LocalStorageService {

    /**
     * 本地存储数据访问接口
     */
    @Autowired
    private LocalStorageRepository localStorageRepository;

    /**
     * 本地存储实体与本地存储传输对象映射
     */
    @Autowired
    private LocalStorageMapper localStorageMapper;

    /**
     * 系统文件属性配置
     */
    @Autowired
    private FileProperties properties;

    @Override
    public Object queryAll(LocalStorageQueryCriteria criteria, Pageable pageable) {
        Page<LocalStorage> page = localStorageRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(localStorageMapper::toDto));
    }

    @Override
    public List<LocalStorageDto> queryAll(LocalStorageQueryCriteria criteria) {
        return localStorageMapper.toDto(localStorageRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public LocalStorageDto findById(Long id) {
        LocalStorage localStorage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
        ValidationUtils.isNull(localStorage.getId(), "LocalStorage", "id", id);
        return localStorageMapper.toDto(localStorage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocalStorage create(String name, MultipartFile multipartFile) {
        FileUtils.checkSize(properties.getMaxSize(), multipartFile.getSize());
        String suffix = FileUtils.getExtensionName(multipartFile.getOriginalFilename());
        String type = FileUtils.getFileType(suffix);
        File file = FileUtils.upload(multipartFile, properties.getPath().getPath() + type + File.separator);
        if (ObjectUtil.isNull(file)) {
            throw new BadRequestException("上传失败");
        }
        try {
            name = StringUtils.isBlank(name) ? FileUtils.getFileNameNoEx(multipartFile.getOriginalFilename()) : name;
            LocalStorage localStorage = new LocalStorage(file.getName(), name, suffix, file.getPath(), type,
                    FileUtils.getSize(multipartFile.getSize()));
            return localStorageRepository.save(localStorage);
        } catch (Exception e) {
            FileUtils.del(file);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(LocalStorage resources) {
        LocalStorage localStorage = localStorageRepository.findById(resources.getId()).orElseGet(LocalStorage::new);
        ValidationUtils.isNull(localStorage.getId(), "LocalStorage", "id", resources.getId());
        localStorage.copy(resources);
        localStorageRepository.save(localStorage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            LocalStorage storage = localStorageRepository.findById(id).orElseGet(LocalStorage::new);
            FileUtils.del(storage.getPath());
            localStorageRepository.delete(storage);
        }
    }

    @Override
    public void download(List<LocalStorageDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (LocalStorageDto localStorageDTO : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("文件名", localStorageDTO.getRealName());
            map.put("备注名", localStorageDTO.getName());
            map.put("文件类型", localStorageDTO.getType());
            map.put("文件大小", localStorageDTO.getSize());
            map.put("创建者", localStorageDTO.getCreateBy());
            map.put("创建日期", localStorageDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
