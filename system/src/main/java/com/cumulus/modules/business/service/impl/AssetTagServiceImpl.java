package com.cumulus.modules.business.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;

import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.dto.AssetQueryCriteria;
import com.cumulus.modules.business.dto.AssetTagDto;
import com.cumulus.modules.business.dto.AssetTagListTreeDto;
import com.cumulus.modules.business.dto.AssetTagTreeDto;
import com.cumulus.modules.business.dto.ImportResultDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetTag;
import com.cumulus.modules.business.mapstruct.AssetTagListTreeMapper;
import com.cumulus.modules.business.mapstruct.AssetTagMapper;
import com.cumulus.modules.business.mapstruct.AssetTagTreeMapper;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.AssetTagRepository;
import com.cumulus.modules.business.service.AssetTagService;
import com.cumulus.utils.ExcelResolve;
import com.cumulus.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资产标签服务实现类
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class AssetTagServiceImpl implements AssetTagService {

    /**
     * 资产标签批量导入excel表头
     */
    private static final String EXCEL_HEADER_TAG_PARENT = "*资产标签";
    private static final String EXCEL_HEADER_TAG_NUMBER_PARENT = "一级编号（选填）";
    private static final String EXCEL_HEADER_TAG_DESCRIPTION_PARENT = "一级备注";
    private static final String EXCEL_HEADER_TAG_NAME_SUB = "资产子标签";
    private static final String EXCEL_HEADER_TAG_NUMBER_SUB = "二级编号（选填）";
    private static final String EXCEL_HEADER_TAG_DESCRIPTION_SUB = "二级备注";

    /**
     * 资产数据访问接口
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * 资产标签数据访问接口
     */
    @Autowired
    private AssetTagRepository assetTagRepository;

    /**
     * 资产标签传输对象与资产标签实体的映射
     */
    @Resource
    private AssetTagMapper assetTagMapper;

    /**
     * assetTagTreeDto与资产标签实体的映射
     */
    @Resource
    private AssetTagTreeMapper assetTagTreeMapper;

    /**
     * assetTagListTreeDto与资产标签实体的映射
     */
    @Resource
    private AssetTagListTreeMapper assetTagListTreeMapper;

    /**
     * 批量新增
     *
     * @param file 标签列表
     * @return 标签列表
     */
    @Override
    public Object createBatch(MultipartFile file) {
        long start = System.currentTimeMillis();
        if (file == null) {
            throw new BadRequestException("文件不存在");
        }
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        Map<String, AssetTag> assetTagMapParent = new HashMap<>(8);
        List<AssetTag> assetTagListSub = new ArrayList<>();
        List<AssetTagDto> assetTagDtoList = new ArrayList<>();
        try {
            ExcelResolve.resolveExcel(file.getInputStream(), (headMap, row) -> {
                AssetTag assetTagParent = new AssetTag();
                AssetTag assetTagSub = new AssetTag();
                String parentName = ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_PARENT)));
                if (parentName == null) {
                    AssetTagDto assetTagDto = new AssetTagDto();
                    assetTagDto.setResult("新增失败, 父类标签为空");
                    fail.incrementAndGet();
                    assetTagDtoList.add(assetTagDto);
                    return;
                }
                assetTagParent.setName(parentName);
                assetTagParent.setNumber(ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_NUMBER_PARENT))));
                assetTagParent.setDescription(ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_DESCRIPTION_PARENT))));
                assetTagMapParent.putIfAbsent(parentName, assetTagParent);
                String subName = ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_NAME_SUB)));
                if (subName == null || StringUtils.isBlank(subName)) {
                    return;
                }
                assetTagSub.setName(subName);
                assetTagSub.setNumber(ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_NUMBER_SUB))));
                assetTagSub.setDescription(ExcelResolve.getCellValue(row.getCell(headMap.get(EXCEL_HEADER_TAG_DESCRIPTION_SUB))));
                assetTagSub.setParent(assetTagParent);
                assetTagListSub.add(assetTagSub);
            });
        } catch (Exception e) {
            throw new BadRequestException("文件解析失败");
        }
        for (AssetTag tag : assetTagMapParent.values()) {
            AssetTagDto assetTagDto = assetTagMapper.toDto(tag);
            try {
                create(tag);
                success.incrementAndGet();
                assetTagDto.setResult("成功");
                assetTagDtoList.add(assetTagDto);
            } catch (BadRequestException exception) {
                fail.incrementAndGet();
                assetTagDto.setResult(exception.getMessage());
                assetTagDtoList.add(assetTagDto);
            }
        }
        for (AssetTag tag : assetTagListSub) {
            AssetTagDto assetTagDto = assetTagMapper.toDto(tag);
            Optional<AssetTag> parentOpt = assetTagRepository.findByName(tag.getParent().getName());
            if (!parentOpt.isPresent()) {
                fail.incrementAndGet();
                assetTagDto.setResult("父类标签不存在");
            } else {
                tag.setParent(parentOpt.get());
                try {
                    create(tag);
                    assetTagDto.setResult("成功");
                    success.incrementAndGet();
                } catch (BadRequestException exception) {
                    fail.incrementAndGet();
                    assetTagDto.setResult(exception.getMessage());
                }
            }
            assetTagDtoList.add(assetTagDto);
        }
        log.info("resolve excel took {}", System.currentTimeMillis() - start);
        return new ImportResultDto<>(new ImportResultDto.ImportResult(success.get() + fail.get(), success.get(), fail.get())
                , assetTagDtoList);
    }

    /**
     * 添加标签
     *
     * @param assetTag 标签传输对象
     */
    @Override
    public void create(AssetTag assetTag) {
        if (assetTag.getId() != null) {
            throw new BadRequestException("新增失败,新增资产标签不能存在id");
        }
        String message = checkTagCreate(assetTag);
        assetTag.setCustomize(false);
        assetTag.setEnabled(true);
        if (message != null) {
            throw new BadRequestException("新增失败" + message);
        }
        assetTagRepository.save(assetTag);
    }

    /**
     * 分页查询资产标签
     *
     * @param pageable 分页参数
     * @return 资产标签列表
     */
    @Override
    public Object queryAll(Pageable pageable) {
        Page<AssetTag> assetTagPage = assetTagRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("customize"), false), pageable);
        return assetTagPage.map(assetTagMapper::toDto);
    }

    @Override
    public Page<AssetTagTreeDto> queryTreePage(Pageable pageable) {
        Page<AssetTag> page = assetTagRepository.findAllByParentIsNullAndEnabledIsTrue(pageable);
        return page.map(assetTagTreeMapper::toDto);
    }

    @Override
    public Page<AssetTag> queryTreePageSub(Long parentTagId, Pageable pageable) {
        AssetTag assetTag = new AssetTag();
        assetTag.setId(parentTagId);
        return assetTagRepository.findAllByParentAndEnabledIsTrue(assetTag, pageable);
    }

    /**
     * 分页查询资产标签
     *
     * @return 资产标签列表
     */
    @Override
    public List<AssetTagListTreeDto> querySelect() {
        List<AssetTag> assetTags = assetTagRepository.findAllByParentIsNullAndEnabledIsTrue();
        return assetTagListTreeMapper.toDto(assetTags);
    }

    /**
     * 根据id删除
     *
     * @param tagId 标签id
     */
    @Override
    public void removeById(Long tagId) {
        if (tagId == null) {
            throw new BadRequestException("删除失败id为空");
        }
        Optional<AssetTag> tagOpt = assetTagRepository.findById(tagId);
        if (!tagOpt.isPresent()) {
            throw new BadRequestException("标签不存在");
        }
        AssetTag assetTag = tagOpt.get();
        if (assetTag.getCustomize()) {
            throw new BadRequestException("内置标签无法删除");
        }
        Long count = assetTagRepository.countAllByParent(assetTag);
        if (count > 0) {
            throw new BadRequestException("存在子类无法删除");
        }
        assetTagRepository.deleteById(tagId);
    }

    /**
     * 批量删除
     *
     * @param ids    id列表
     * @param delAll 是否全部删除
     * @return 删除结果
     */
    @Override
    public String removeBatch(Set<Long> ids, Boolean delAll) {
        List<AssetTag> assetTagList;
        if (delAll) {
            assetTagList = assetTagRepository.findAllByCustomizeFalse();
        } else {
            assetTagList = assetTagRepository.findAllById(ids);
        }
        AtomicInteger success = new AtomicInteger();
        //先删除子类标签 统一使用 removeById 方法
        assetTagList.stream().filter(assetTag -> assetTag.getParent() != null).forEach(assetTag -> {
            try {
                removeById(assetTag.getId());
                success.incrementAndGet();
            } catch (BadRequestException ignored) {
            }
        });
        //再删除父类标签
        assetTagList.stream().filter(assetTag -> assetTag.getParent() == null).forEach(assetTag -> {
            try {
                removeById(assetTag.getId());
                success.incrementAndGet();
            } catch (BadRequestException ignored) {
            }
        });
        return String.format("成功删除%d个标签", success.get());
    }

    /**
     * 当前标签已使用就 修改资产标签到其他
     *
     * @param id  单个id
     * @param ids id列表
     */
    private void updateTagToOther(Long id, Set<Long> ids) {
        AssetQueryCriteria criteria = new AssetQueryCriteria();
        if (ids.isEmpty()) {
            ids.add(id);
        }
        criteria.setAssetTagIds(ids);
        //当前标签的资产列表
        List<Asset> assetList = assetRepository.findAll((root, query, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder));
        //其他标签
        AssetTag other = assetTagRepository.findAssetTagByNameEquals("其他");
        if (assetList.isEmpty()) {
            assetList.forEach(asset -> {
                asset.setAssetTags(new HashSet<>(Collections.singleton(other)));
            });
            assetRepository.saveAll(assetList);
        }
    }

    /**
     * 修改资产标签
     *
     * @param assetTag 资产标签传输对象
     */
    @Override
    public void updateById(AssetTag assetTag) {
        String message = checkTagUpdate(assetTag);
        if (message != null) {
            throw new BadRequestException("更新失败," + message);
        }
        assetTagRepository.save(assetTag);
    }

    /**
     * 根据标签名字查重
     *
     * @param newTag 新标签对象
     */
    private String checkTagUpdate(AssetTag newTag) {
        if (newTag.getId() == null) {
            throw new BadRequestException("id不能为空");
        }
        Optional<AssetTag> oldTagOpt = assetTagRepository.findById(newTag.getId());
        if (!oldTagOpt.isPresent()) {
            throw new BadRequestException("标签不存在");
        }
        AssetTag oldTag = oldTagOpt.get();
        if (oldTag.getCustomize()){
            throw new BadRequestException("内置标签无法修改");
        }
        if (StringUtils.isBlank(newTag.getName())) {
            newTag.setName(oldTag.getName());
        }
        if (newTag.getNumber() == null) {
            newTag.setNumber(oldTag.getNumber());
        }
        if (newTag.getDescription() == null) {
            oldTag.setDescription(newTag.getDescription());
        }
        newTag.setParent(oldTag.getParent());
        newTag.setCustomize(oldTag.getCustomize());
        newTag.setEnabled(oldTag.getEnabled());
        return checkTagCreate(oldTag);
    }

    /**
     * 根据标签名字查重
     *
     * @param tag 新标签对象
     */
    private String checkTagCreate(AssetTag tag) {
        //校验标签名
        if (StringUtils.isBlank(tag.getName())) {
            return "标签名不能为空";
        }
        Optional<AssetTag> assetTag = assetTagRepository.findByName(tag.getName());
        if (assetTag.isPresent() && (tag.getId() == null || !assetTag.get().getId().equals(tag.getId()))) {
            return "标签名重复";
        }
        //校验标签编号
        if (StringUtils.isNotBlank(tag.getNumber())) {
            assetTag = assetTagRepository.findByNumber(tag.getNumber());
            if (assetTag.isPresent() && (tag.getId() == null || !assetTag.get().getId().equals(tag.getId()))) {
                return "标签编号重复";
            }
        } else {
            tag.setNumber(null);
        }
        //校验标签父类
        if (tag.getParent() != null && tag.getParent().getId() != null) {
            assetTag = assetTagRepository.findById(tag.getParent().getId());
            if (!assetTag.isPresent()) {
                return "父类标签不存在";
            } else if (assetTag.get().getParent() != null) {
                return "父类标签是二级标签";
            }
        } else {
            tag.setParent(null);
        }
        return null;
    }
}
