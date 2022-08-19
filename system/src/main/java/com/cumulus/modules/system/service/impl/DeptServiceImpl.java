package com.cumulus.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cumulus.enums.DataScopeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.exception.EntityExistException;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.modules.system.dto.DeptDto;
import com.cumulus.modules.system.dto.DeptQueryCriteria;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.entity.Job;
import com.cumulus.modules.system.entity.User;
import com.cumulus.modules.system.mapstruct.DeptMapper;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.modules.system.repository.UserRepository;
import com.cumulus.modules.system.service.DeptService;
import com.cumulus.utils.CacheKey;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.RegexUtil;
import com.cumulus.utils.SecurityUtils;
import com.cumulus.utils.StringUtils;
import com.cumulus.utils.ValidationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author shenjc
 */
@Service
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

    /**
     * 部门数据访问接口
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * 系统部门传输对象与系统部门实体的映射
     */
    @Lazy
    @Autowired
    private DeptMapper deptMapper;

    /**
     * 用户数据访问接口
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * 资产数据接口
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * ip库数据访问接口
     */
    @Autowired
    private IpLibraryRepository ipLibraryRepository;

    /**
     * redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
        Sort sort = Sort.by(Sort.Direction.ASC, "deptSort");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if (dataScopeType.equals(DataScopeEnum.ALL.getValue())) {
                criteria.setPidIsNull(true);
            }
            List<Field> fields = QueryUtils.getAllFields(criteria.getClass(), new ArrayList<>());
            List<String> fieldNames = new ArrayList<String>() {

                private static final long serialVersionUID = 2879882117402507111L;

                {
                    add("pidIsNull");
                    add("enabled");
                }
            };
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if (fieldNames.contains(field.getName())) {
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        List<DeptDto> list = deptMapper.toDto(deptRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重
        if (StringUtils.isBlank(dataScopeType)) {
            return deduplication(list);
        }
        return list;
    }

    @Override
    public Page<DeptDto> queryAll(DeptQueryCriteria criteria, Pageable pageable) {
        criteria.setPidIsNull(criteria.getPid() == null ? true : null);
        Page<Dept> page = deptRepository.findAll(
                (root, criteriaQuery, criteriaBuilder) -> QueryUtils.getPredicate(root, criteria, criteriaBuilder),
                pageable);
        return page.map(deptMapper::toDto);
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    public DeptDto findById(Long id) {
        Dept dept = deptRepository.findById(id).orElseGet(Dept::new);
        ValidationUtils.isNull(dept.getId(), "Dept", "id", id);
        return deptMapper.toDto(dept);
    }

    @Override
    public List<Dept> findByPid(long pid) {
        return deptRepository.findByPid(pid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dept resources) {
        checkCreate(resources);
        resources.setEnabled(resources.getEnabled() == null ? Dept.ENABLE : resources.getEnabled());
        resources.setDeptSort(resources.getDeptSort() == null ? Dept.DEFAULT_DEPT_SORT : resources.getDeptSort());
        deptRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dept resources) {
        Dept oldDept = deptRepository.findById(resources.getId()).orElseGet(Dept::new);
        checkUpdate(resources, oldDept);
        deptRepository.save(oldDept);
        // 更新父节点中子节点数目
        if (oldDept.getPid() != null || resources.getPid() != null) {
            updateSubCnt(oldDept.getPid());
            updateSubCnt(resources.getPid());
        }
        // 清理缓存
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<DeptDto> deptDtos) {
        for (DeptDto deptDto : deptDtos) {
            // 清理缓存
            delCaches(deptDto.getId());
            deptRepository.deleteById(deptDto.getId());
            userRepository.deleteByDept(deptMapper.toEntity(deptDto));
            updateSubCnt(deptDto.getPid());
        }
    }

    @Override
    public void download(List<DeptDto> deptDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeptDto deptDTO : deptDtos) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("部门名称", deptDTO.getName());
            map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    @Override
    public Set<DeptDto> getDeleteDepts(List<Dept> menuList, Set<DeptDto> deptDtos) {
        for (Dept dept : menuList) {
            deptDtos.add(deptMapper.toDto(dept));
            List<Dept> depts = deptRepository.findByPid(dept.getId());
            if (null != depts && depts.size() != 0) {
                getDeleteDepts(depts, deptDtos);
            }
        }
        return deptDtos;
    }

    @Override
    public List<Long> getDeptChildren(List<Dept> deptList) {
        List<Long> list = new ArrayList<>();
        deptList.forEach(dept -> {
                    if (null != dept && dept.getEnabled()) {
                        List<Dept> depts = deptRepository.findByPid(dept.getId());
                        if (depts.size() != 0) {
                            list.addAll(getDeptChildren(depts));
                        }
                        list.add(dept.getId());
                    }
                }
        );
        return list;
    }

    @Override
    public List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts) {
        if (null == deptDto.getPid()) {
            depts.addAll(deptRepository.findByPidIsNull());
            return deptMapper.toDto(depts);
        }
        depts.addAll(deptRepository.findByPid(deptDto.getPid()));
        return getSuperior(findById(deptDto.getPid()), depts);
    }

    @Override
    public Object buildTree(List<DeptDto> deptDtos) {
        Set<DeptDto> trees = new LinkedHashSet<>();
        Set<DeptDto> depts = new LinkedHashSet<>();
        List<String> deptNames = deptDtos.stream().map(DeptDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (DeptDto deptDTO : deptDtos) {
            isChild = false;
            if (null == deptDTO.getPid()) {
                trees.add(deptDTO);
            }
            for (DeptDto it : deptDtos) {
                if (it.getPid() != null && deptDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (deptDTO.getChildren() == null) {
                        deptDTO.setChildren(new ArrayList<>());
                    }
                    deptDTO.getChildren().add(it);
                }
            }
            if (isChild) {
                depts.add(deptDTO);
            } else if (null != deptDTO.getPid() && !deptNames.contains(findById(deptDTO.getPid()).getName())) {
                depts.add(deptDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String, Object> map = new HashMap<>(2);
        map.put("totalElements", deptDtos.size());
        map.put("content", CollectionUtil.isEmpty(trees) ? deptDtos : trees);
        return map;
    }

    @Override
    public void verification(Set<DeptDto> deptDtos) {
        Set<Long> deptIds = deptDtos.stream().map(DeptDto::getId).collect(Collectors.toSet());
        if (userRepository.countByDepts(deptIds, new HashSet<>(Collections.singletonList(Job.DEFAULT_DEPT_HEAD_JOB_ID))) > 0) {
            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
        }
        List<Dept> collect = deptDtos.stream().map(deptMapper::toEntity).collect(Collectors.toList());
        if (assetRepository.countAllByDeptIn(collect) != 0){
            throw new BadRequestException("所选部门存在资产关联，请解除后再试！");
        }
        if (ipLibraryRepository.countAllByDeptIn(collect) != 0){
            throw new BadRequestException("所选部门存在IP库关联，请解除后再试！");
        }
    }

    @Override
    public Optional<Dept> findByDeptName(String deptName) {
        List<Dept> deptList = deptRepository.findAllByName(deptName);
        if (deptList.isEmpty()){
            return Optional.empty();
        }
        return Optional.of(deptList.get(0));
    }

    /**
     * 跟新子部门数量
     *
     * @param deptId 部门ID
     */
    private void updateSubCnt(Long deptId) {
        if (null != deptId) {
            int count = deptRepository.countByPid(deptId);
            deptRepository.updateSubCntById(count, deptId);
        }
    }

    /**
     * 部门列表去重
     *
     * @param list 部门列表
     * @return 部门列表
     */
    private List<DeptDto> deduplication(List<DeptDto> list) {
        List<DeptDto> deptDtos = new ArrayList<>();
        for (DeptDto deptDto : list) {
            boolean flag = true;
            for (DeptDto dto : list) {
                if (dto.getId().equals(deptDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                deptDtos.add(deptDto);
            }
        }
        return deptDtos;
    }

    /**
     * 清理缓存
     *
     * @param id 部门ID
     */
    public void delCaches(Long id) {
        if (id == null) {
            return;
        }
        Dept dept = new Dept();
        dept.setId(id);
        List<User> users = userRepository.findAllByDept(dept);
        // 删除数据权限
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);
    }

    /**
     * 校验新增的部门
     *
     * @param resources 新增的部门对象
     */
    private void checkCreate(Dept resources) {
        if (!RegexUtil.regexHexLengthAndBlank(Dept.MAX_NAME_SIZE, 0, false, resources.getName())) {
            throw new BadRequestException("部门名长度不正确");
        }
        List<Dept> allByName = deptRepository.findAllByName(resources.getName());
        if (null != allByName && !allByName.isEmpty()) {
            throw new BadRequestException("部门名重复");
        }
    }

    /**
     * 校验更新的部门对象
     *
     * @param newDept 新的部门对象
     * @param oldDept 旧的部门对象
     */
    private void checkUpdate(Dept newDept, Dept oldDept) {
        List<Dept> allByName = deptRepository.findAllByName(newDept.getName());
        if (null != allByName && !allByName.isEmpty() && !allByName.get(0).getId().equals(oldDept.getId())) {
            throw new BadRequestException("部门名已存在");
        }
        oldDept.setName(newDept.getName());
        if (newDept.getId().equals(newDept.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        oldDept.setPid(newDept.getPid());
    }
}
