package com.cumulus.modules.business.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.alibaba.fastjson.JSONArray;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.dto.AssetSysTypeDto;
import com.cumulus.modules.business.dto.AssetTypeDto;
import com.cumulus.modules.business.dto.ImportResultDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetSysType;
import com.cumulus.modules.business.entity.AssetType;
import com.cumulus.modules.business.mapstruct.AssetSysTypeMapper;
import com.cumulus.modules.business.mapstruct.AssetTypeMapper;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetSysTypeRepository;
import com.cumulus.modules.business.repository.AssetTypeRepository;
import com.cumulus.modules.business.service.AssetTypeService;
import com.cumulus.utils.ExcelResolve;
import com.cumulus.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产类型服务实现
 *
 * @author zhangxq
 */
@Service
@Slf4j
public class AssetTypeServiceImpl implements AssetTypeService {

    /**
     * 资产数据类型访问接口
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * 资产类型数据访问接口
     */
    @Resource
    private AssetTypeRepository assetTypeRepository;

    /**
     * 子资产类型数据访问接口
     */
    @Resource
    private AssetSysTypeRepository assetSysTypeRepository;

    /**
     * 资产类型传输对象与资产类型实体的映射
     */
    @Resource
    private AssetTypeMapper assetTypeMapper;

    /**
     * 子资产类型传输对象与子资产类型实体的映射
     */
    @Resource
    private AssetSysTypeMapper assetSysTypeMapper;

    /**
     * 批量加入
     *
     * @param file 资产类型列表
     * @return 批量录入结果
     */
    @Override
    public Object createBatch(MultipartFile file) {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = new ExcelResolve().readExcel(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ImportResultDto importResultDto = new ImportResultDto();
        importResultDto.getResult().setSum(jsonArray.size());
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<AssetSysTypeDto> assetSysTypeDtoList = new ArrayList<>();
        if (jsonArray.isEmpty()) {
            log.warn("excel为空");
            return importResultDto;
        }
        //父类型查重map
        HashMap<String, Boolean> hm = new HashMap();
        jsonArray.forEach(obj -> {
            Map map = objectMapper.convertValue(obj, Map.class);
            AssetSysTypeDto assetSysTypeDto = new AssetSysTypeDto();
            assetSysTypeDto.setParent(map.get("*资产类型名称").toString());
            assetSysTypeDto.setParentNumber(map.get("一级编号（选填）").toString());
            assetSysTypeDto.setName(map.get("资产子类型名称").toString());
            assetSysTypeDto.setNumber(map.get("二级编号（选填）").toString());
            assetSysTypeDto.setDescription(map.get("备注").toString());
            //子类型为空则添加父类型
            if ("".equals(assetSysTypeDto.getName())) {
                //添加父类型到map作查重
                if (!hm.containsKey(assetSysTypeDto.getParent())) {
                    hm.put(assetSysTypeDto.getParent(), true);
                }
                if (checkTypeName(assetSysTypeDto.getParent())) {
                    AssetType assetType = new AssetType();
                    assetType.setName(assetSysTypeDto.getParent());
                    assetType.setNumber(assetSysTypeDto.getParentNumber());
                    assetType.setDescription(assetSysTypeDto.getDescription());
                    assetTypeRepository.save(assetType);
                    assetSysTypeDto.setResult("添加成功");
                    success.getAndIncrement();
                } else {
                    assetSysTypeDto.setResult("标签重复");
                    fail.getAndIncrement();
                }
            } else {
                //如果没有则添加父类型到map
                if (!hm.containsKey(assetSysTypeDto.getParent())) {
                    hm.put(assetSysTypeDto.getParent(), true);
                    AssetSysTypeDto parentDto = new AssetSysTypeDto();
                    parentDto.setParent(assetSysTypeDto.getParent());
                    assetSysTypeDtoList.add(parentDto);
                }
                //否则 添加子类型
                if (checkSysTypeName(assetSysTypeDto.getName())) {
                    AssetType assetType = assetTypeRepository.findAssetTypeByNameEquals(assetSysTypeDto.getParent());
                    if (assetType == null) {
                        assetType = new AssetType();
                        assetType.setName(assetSysTypeDto.getParent());
                        assetType.setNumber(assetSysTypeDto.getParentNumber());
                        assetType = assetTypeRepository.save(assetType);
                        assetSysTypeDto.setResult("添加成功");
                        success.getAndIncrement();
                    }
                    AssetSysType assetSysType = assetSysTypeMapper.toEntity(assetSysTypeDto);
                    assetSysType.setAssetType(assetType);
                    assetSysTypeRepository.save(assetSysType);
                    assetSysTypeDto.setResult("添加成功");
                    success.getAndIncrement();
                } else {
                    assetSysTypeDto.setResult("标签重复");
                    fail.getAndIncrement();
                }
            }
            assetSysTypeDtoList.add(assetSysTypeDto);
        });
        //父类型列表
        List<AssetSysTypeDto> parentList =
                assetSysTypeDtoList.stream().filter(o -> StringUtils.isEmpty(o.getName())).collect(Collectors.toList());
        //构建树结构
        parentList.forEach(p -> {
            List<AssetSysTypeDto> childList = new ArrayList<>();
            assetSysTypeDtoList.forEach(o -> {
                if (!StringUtils.isEmpty(o.getName()) && p.getParent().equals(o.getParent())) {
                    childList.add(o);
                }
            });
            if (!childList.isEmpty()) {
                p.setChildrenType(childList);
            }
        });
        importResultDto.getResult().setSuccess(success.get());
        importResultDto.getResult().setFail(fail.get());
        importResultDto.setObjectList(parentList);
        return importResultDto;
    }

    /**
     * 新增子资产类型
     *
     * @param assetSysTypeDto 资产类型对象
     */
    @Override
    public void create(AssetSysTypeDto assetSysTypeDto) {
        if (checkSysTypeName(assetSysTypeDto.getName())) {
            assetSysTypeRepository.save(assetSysTypeMapper.toEntity(assetSysTypeDto));
        } else {
            throw new BadRequestException("当前类型名称已存在");
        }
    }

    /**
     * 新增父资产类型
     *
     * @param assetTypeDto 父资产类型
     */
    @Override
    public void create(AssetTypeDto assetTypeDto) {
        if (checkTypeName(assetTypeDto.getName())) {
            assetTypeRepository.save(assetTypeMapper.toEntity(assetTypeDto));
        } else {
            throw new BadRequestException("当前类型名称已存在");
        }
    }

    /**
     * 分页查询资产类型
     *
     * @param pageable 分页参数
     * @return 资产类型列表
     */
    @Override
    public Object queryAll(Pageable pageable) {
        Page<AssetType> assetTypePage = assetTypeRepository.findAll(pageable);
        if (assetTypePage.isEmpty()) {
            return null;
        }
        Page<AssetTypeDto> assetTypeDtoPage = assetTypePage.map(assetTypeMapper::toDto);
        assetTypeDtoPage.forEach(assetTypeDto ->
                assetTypeDto.setChildrenType(
                        assetSysTypeRepository.findAssetSysTypesByAssetTypeEquals(
                                assetTypeMapper.toEntity(assetTypeDto),
                                PageRequest.of(0, 10)))
        );
        return assetTypeDtoPage;
    }

    /**
     * 查询全部
     *
     * @return 资产类型
     */
    @Override
    public Object querySelecct() {
        List<AssetType> typeList = assetTypeRepository.findAll();
        if (typeList.isEmpty()) {
            return null;
        }
        List<AssetTypeDto> typeDtos = typeList.stream().map(assetTypeMapper::toDto).collect(Collectors.toList());
        typeDtos.forEach(assetTypeDto ->
                assetTypeDto.setChildrenType(
                        assetSysTypeRepository.findAssetSysTypesByAssetTypeEquals(
                                assetTypeMapper.toEntity(assetTypeDto),
                                PageRequest.of(0, 999)))
        );
        return typeDtos;
    }

    /**
     * 分页查询资产类型
     *
     * @param id       父id
     * @param pageable 分页参数
     * @return 资产类型列表
     */
    @Override
    public Object queryChild(Integer id, Pageable pageable) {
        AssetType assetType = new AssetType();
        assetType.setId(id);
        return assetSysTypeRepository.findAssetSysTypesByAssetTypeEquals(assetType, pageable);
    }

    /**
     * 根据id删除
     *
     * @param id id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeTypeById(Integer id) {
        AssetType assetType = assetTypeRepository.findById(id).orElse(null);
        if (assetType == null) {
            throw new BadRequestException("删除失败，类型不存在");
        }
        Set<Integer> assetTypes = new HashSet<>();
        assetTypes.add(assetType.getId());
        if (assetRepository.countAllByAssetTypeIsIn(assetTypes) > 0) {
            updateTypeToOther(id, true);
            //有资产则修改资产到其他
        } else if (assetSysTypeRepository.countAssetSysTypesByAssetTypeEquals(assetType) > 0) {
            throw new BadRequestException("删除失败，包含子资产类型");
        }
        assetTypeRepository.deleteById(id);
    }

    /**
     * 删除修改类型到其他
     *
     * @param id           类型id
     * @param isParentType 是否为父类型
     */
    private void updateTypeToOther(Integer id, boolean isParentType) {
        //其他类型
        AssetType other = assetTypeRepository.findAssetTypeByNameEquals("其他");
        //当前类型引用资产列表
        List<Asset> assetList;
        //true 父类型 assetType
        if (isParentType) {
            AssetType assetType = assetTypeRepository.findById(id).orElse(null);
            assetList = assetRepository.findAllByAssetTypeEquals(assetType);
        } else {
            //false 子类型 assetSysType
            AssetSysType assetSysType = assetSysTypeRepository.findById(id).orElse(null);
            assetList = assetRepository.findAllByAssetSysTypeEquals(assetSysType);
        }
        //不为空则挨个修改
        if (!assetList.isEmpty()) {
            assetList.forEach(asset -> {
                asset.setAssetType(other);
                asset.setAssetSysType(null);
            });
            assetRepository.saveAll(assetList);
        }
    }

    /**
     * 删除子类型
     *
     * @param id 子类型id
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeSysTypeById(Integer id) {
        AssetSysType assetSysType = assetSysTypeRepository.findById(id).orElse(null);
        if (assetSysType == null) {
            throw new BadRequestException("删除失败，类型不存在");
        }
        Set<Integer> assetSysTypes = new HashSet<>();
        assetSysTypes.add(assetSysType.getId());
        //有资产则修改资产到其他
        if (assetRepository.countAllByAssetSysTypeIsIn(assetSysTypes) > 0) {
            updateTypeToOther(id, true);
        }
        assetSysTypeRepository.deleteById(id);
    }


    /**
     * 批量删除
     *
     * @param ids id列表
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatchType(Set<Integer> ids, Boolean delAll) {
        if (delAll) {
            assetTypeRepository.findAll((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customize"), false)).forEach(e ->
                    this.removeTypeById(e.getId())
            );
        } else {
            ids.forEach(this::removeTypeById);
        }
    }

    /**
     * 批量删除
     *
     * @param ids    id列表
     * @param delAll 是否全部删除
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatchSysType(Set<Integer> ids, Boolean delAll) {
        if (delAll) {
            assetSysTypeRepository.findAll((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customize"), false)).forEach(e -> {
                this.removeSysTypeById(e.getId());
            });
        } else {
            ids.forEach(this::removeSysTypeById);
        }
    }

    /**
     * 全部删除
     *
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delAll() {
        this.removeBatchSysType(new HashSet<>(), true);
        this.removeBatchType(new HashSet<>(), true);
    }

    /**
     * 修改类型
     *
     * @param assetTypeDto 资产类型传输对象
     * @return 修改结果
     */
    @Override
    public void updateById(AssetTypeDto assetTypeDto) {
        assetTypeRepository.save(assetTypeMapper.toEntity(assetTypeDto));
    }

    /**
     * 修改类型
     *
     * @param assetTypeDto 资产类型传输对象
     * @return 修改结果
     */
    @Override
    public void updateSysTypeById(AssetSysTypeDto assetTypeDto) {
        assetSysTypeRepository.save(assetSysTypeMapper.toEntity(assetTypeDto));
    }

    /**
     * 根据类型名字查重
     *
     * @param typeName 类型名称
     * @return true 不重复 false 重复
     */
    private boolean checkTypeName(String typeName) {
        return null == assetTypeRepository.findAssetTypeByNameEquals(typeName);
    }

    /**
     * 根据类型名字查重
     *
     * @param typeName 类型名称
     * @return true 不重复 false 重复
     */
    private boolean checkSysTypeName(String typeName) {
        return null == assetSysTypeRepository.findAssetSysTypeByNameEquals(typeName);
    }

}
