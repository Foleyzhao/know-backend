package com.cumulus.modules.system.service.impl;

import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.modules.system.dto.JobDto;
import com.cumulus.modules.system.dto.JobQueryCriteria;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.mapstruct.JobMapper;
import com.cumulus.modules.system.repository.JobRepository;
import com.cumulus.modules.system.repository.UserRepository;
import com.cumulus.modules.system.service.JobService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
 * 岗位服务实现
 */
@Service
@CacheConfig(cacheNames = "job")
public class JobServiceImpl implements JobService {

    /**
     * 岗位数据访问接口
     */
    @Autowired
    private JobRepository jobRepository;

    /**
     * 系统岗位传输对象与系统岗位实体的映射
     */
    @Autowired
    private JobMapper jobMapper;

    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, Object> queryAll(JobQueryCriteria criteria, Pageable pageable) {
        Page<Job> page = jobRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return PageUtils.toPage(page.map(jobMapper::toDto).getContent(), page.getTotalElements());
    }

    @Override
    public List<JobDto> queryAll(JobQueryCriteria criteria) {
        List<Job> list = jobRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder));
        return jobMapper.toDto(list);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public JobDto findById(Long id) {
        Job job = jobRepository.findById(id).orElseGet(Job::new);
        ValidationUtils.isNull(job.getId(), "Job", "id", id);
        return jobMapper.toDto(job);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Job resources) {
        Job job = jobRepository.findByName(resources.getName());
        if (null != job) {
            throw new BadRequestException("职务名已存在");
        }
        jobRepository.save(resources);
    }

    @Override
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(Job resources) {
        Job job = jobRepository.findById(resources.getId()).orElseGet(Job::new);
        Job old = jobRepository.findByName(resources.getName());
        if (null != old && !old.getId().equals(resources.getId())) {
            throw new BadRequestException("职务名已存在");
        }
        ValidationUtils.isNull(job.getId(), "Job", "id", resources.getId());
        resources.setId(job.getId());
        jobRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        jobRepository.deleteAllByIdIn(ids);
        // 删除缓存
        redisUtils.delByKeys(CacheKey.JOB_ID, ids);
    }

    @Override
    public void download(List<JobDto> jobDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobDto jobDTO : jobDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("岗位名称", jobDTO.getName());
            map.put("岗位状态", jobDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", jobDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    @Override
    public void verification(Set<Long> ids) {
        if (userRepository.countByJobs(ids) > 0) {
            throw new BadRequestException("所选的岗位中存在用户关联，请解除关联再试！");
        }
    }
}
