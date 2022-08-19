package com.cumulus.modules.mnt.service.impl;

import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.mnt.dto.AppDto;
import com.cumulus.modules.mnt.dto.AppQueryCriteria;
import com.cumulus.modules.mnt.entity.App;
import com.cumulus.modules.mnt.mapstruct.AppMapper;
import com.cumulus.modules.mnt.repository.AppRepository;
import com.cumulus.modules.mnt.service.AppService;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.ValidationUtils;
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
 * 应用服务实现
 */
@Service
public class AppServiceImpl implements AppService {

    /**
     * 应用数据访问接口
     */
    @Autowired
    private AppRepository appRepository;

    /**
     * 应用传输对象与应用实体的映射
     */
    @Autowired
    private AppMapper appMapper;

    @Override
    public Object queryAll(AppQueryCriteria criteria, Pageable pageable) {
        Page<App> page = appRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(appMapper::toDto));
    }

    @Override
    public List<AppDto> queryAll(AppQueryCriteria criteria) {
        return appMapper.toDto(appRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public AppDto findById(Long id) {
        App app = appRepository.findById(id).orElseGet(App::new);
        ValidationUtils.isNull(app.getId(), "App", "id", id);
        return appMapper.toDto(app);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(App resources) {
        verification(resources);
        appRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(App resources) {
        verification(resources);
        App app = appRepository.findById(resources.getId()).orElseGet(App::new);
        ValidationUtils.isNull(app.getId(), "App", "id", resources.getId());
        app.copy(resources);
        appRepository.save(app);
    }

    /**
     * 应用路径校验
     *
     * @param resources 应用
     */
    private void verification(App resources) {
        String opt = "/opt";
        String home = "/home";
        if (!(resources.getUploadPath().startsWith(opt) || resources.getUploadPath().startsWith(home))) {
            throw new BadRequestException("文件只能上传在opt目录或者home目录 ");
        }
        if (!(resources.getDeployPath().startsWith(opt) || resources.getDeployPath().startsWith(home))) {
            throw new BadRequestException("文件只能部署在opt目录或者home目录 ");
        }
        if (!(resources.getBackupPath().startsWith(opt) || resources.getBackupPath().startsWith(home))) {
            throw new BadRequestException("文件只能备份在opt目录或者home目录 ");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            appRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<AppDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AppDto appDto : queryAll) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("应用名称", appDto.getName());
            map.put("端口", appDto.getPort());
            map.put("上传目录", appDto.getUploadPath());
            map.put("部署目录", appDto.getDeployPath());
            map.put("备份目录", appDto.getBackupPath());
            map.put("启动脚本", appDto.getStartScript());
            map.put("部署脚本", appDto.getDeployScript());
            map.put("创建日期", appDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
