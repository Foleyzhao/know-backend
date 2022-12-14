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
 * ??????????????????
 *
 * @author shenjc
 */
@Service
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

    /**
     * ????????????????????????
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * ??????????????????????????????????????????????????????
     */
    @Lazy
    @Autowired
    private DeptMapper deptMapper;

    /**
     * ????????????????????????
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * ??????????????????
     */
    @Autowired
    private AssetRepository assetRepository;

    /**
     * ip?????????????????????
     */
    @Autowired
    private IpLibraryRepository ipLibraryRepository;

    /**
     * redis?????????
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
                //???????????????????????????????????????private??????????????????
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
        // ??????????????????????????????????????????????????????????????????????????????
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
        // ?????????????????????
        resources.setSubCount(0);
        // ????????????
        updateSubCnt(resources.getPid());
        // ??????????????????????????????datascope??????
        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dept resources) {
        Dept oldDept = deptRepository.findById(resources.getId()).orElseGet(Dept::new);
        checkUpdate(resources, oldDept);
        deptRepository.save(oldDept);
        // ?????????????????????????????????
        if (oldDept.getPid() != null || resources.getPid() != null) {
            updateSubCnt(oldDept.getPid());
            updateSubCnt(resources.getPid());
        }
        // ????????????
        delCaches(resources.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<DeptDto> deptDtos) {
        for (DeptDto deptDto : deptDtos) {
            // ????????????
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
            map.put("????????????", deptDTO.getName());
            map.put("????????????", deptDTO.getEnabled() ? "??????" : "??????");
            map.put("????????????", deptDTO.getCreateTime());
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
            throw new BadRequestException("??????????????????????????????????????????????????????");
        }
        List<Dept> collect = deptDtos.stream().map(deptMapper::toEntity).collect(Collectors.toList());
        if (assetRepository.countAllByDeptIn(collect) != 0){
            throw new BadRequestException("??????????????????????????????????????????????????????");
        }
        if (ipLibraryRepository.countAllByDeptIn(collect) != 0){
            throw new BadRequestException("??????????????????IP?????????????????????????????????");
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
     * ?????????????????????
     *
     * @param deptId ??????ID
     */
    private void updateSubCnt(Long deptId) {
        if (null != deptId) {
            int count = deptRepository.countByPid(deptId);
            deptRepository.updateSubCntById(count, deptId);
        }
    }

    /**
     * ??????????????????
     *
     * @param list ????????????
     * @return ????????????
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
     * ????????????
     *
     * @param id ??????ID
     */
    public void delCaches(Long id) {
        if (id == null) {
            return;
        }
        Dept dept = new Dept();
        dept.setId(id);
        List<User> users = userRepository.findAllByDept(dept);
        // ??????????????????
        redisUtils.delByKeys(CacheKey.DATA_USER, users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del(CacheKey.DEPT_ID + id);
    }

    /**
     * ?????????????????????
     *
     * @param resources ?????????????????????
     */
    private void checkCreate(Dept resources) {
        if (!RegexUtil.regexHexLengthAndBlank(Dept.MAX_NAME_SIZE, 0, false, resources.getName())) {
            throw new BadRequestException("????????????????????????");
        }
        List<Dept> allByName = deptRepository.findAllByName(resources.getName());
        if (null != allByName && !allByName.isEmpty()) {
            throw new BadRequestException("???????????????");
        }
    }

    /**
     * ???????????????????????????
     *
     * @param newDept ??????????????????
     * @param oldDept ??????????????????
     */
    private void checkUpdate(Dept newDept, Dept oldDept) {
        List<Dept> allByName = deptRepository.findAllByName(newDept.getName());
        if (null != allByName && !allByName.isEmpty() && !allByName.get(0).getId().equals(oldDept.getId())) {
            throw new BadRequestException("??????????????????");
        }
        oldDept.setName(newDept.getName());
        if (newDept.getId().equals(newDept.getPid())) {
            throw new BadRequestException("?????????????????????");
        }
        oldDept.setPid(newDept.getPid());
    }
}
