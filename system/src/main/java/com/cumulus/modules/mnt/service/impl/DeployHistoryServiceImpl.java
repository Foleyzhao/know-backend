package com.cumulus.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import com.cumulus.modules.mnt.entity.DeployHistory;
import com.cumulus.modules.mnt.repository.DeployHistoryRepository;
import com.cumulus.modules.mnt.service.DeployHistoryService;
import com.cumulus.modules.mnt.dto.DeployHistoryDto;
import com.cumulus.modules.mnt.dto.DeployHistoryQueryCriteria;
import com.cumulus.modules.mnt.mapstruct.DeployHistoryMapper;
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
 * 部署历史服务实现
 */
@Service
public class DeployHistoryServiceImpl implements DeployHistoryService {

    /**
     * 部署历史数据访问接口
     */
    @Autowired
    private DeployHistoryRepository deployhistoryRepository;

    /**
     * 部署历史传输对象与部署历史实体的映射
     */
    @Autowired
    private DeployHistoryMapper deployhistoryMapper;

    @Override
    public Object queryAll(DeployHistoryQueryCriteria criteria, Pageable pageable){
        Page<DeployHistory> page = deployhistoryRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root,criteria,criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(deployhistoryMapper::toDto));
    }

    @Override
    public List<DeployHistoryDto> queryAll(DeployHistoryQueryCriteria criteria){
        return deployhistoryMapper.toDto(deployhistoryRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    public DeployHistoryDto findById(String id) {
        DeployHistory deployhistory = deployhistoryRepository.findById(id).orElseGet(DeployHistory::new);
        ValidationUtils.isNull(deployhistory.getId(),"DeployHistory","id",id);
        return deployhistoryMapper.toDto(deployhistory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DeployHistory resources) {
        resources.setId(IdUtil.simpleUUID());
        deployhistoryRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            deployhistoryRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<DeployHistoryDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeployHistoryDto deployHistoryDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部署编号", deployHistoryDto.getDeployId());
            map.put("应用名称", deployHistoryDto.getAppName());
            map.put("部署IP", deployHistoryDto.getIp());
            map.put("部署时间", deployHistoryDto.getDeployDate());
            map.put("部署人员", deployHistoryDto.getDeployUser());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
