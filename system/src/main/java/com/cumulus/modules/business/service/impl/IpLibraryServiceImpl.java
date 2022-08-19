package com.cumulus.modules.business.service.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONArray;
import com.cumulus.constant.FileConstant;
import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.common.DetectManager;
import com.cumulus.modules.business.detect.dto.DetectTaskIpDto;
import com.cumulus.modules.business.detect.service.impl.DetectTaskServiceImpl;
import com.cumulus.modules.business.dto.ImportResultDto;
import com.cumulus.modules.business.dto.IpLibraryDto;
import com.cumulus.modules.business.dto.IpLibraryQueryCriteria;
import com.cumulus.modules.business.entity.IpLibrary;
import com.cumulus.modules.business.mapstruct.IpLibraryMapper;
import com.cumulus.modules.business.repository.AssetRepository;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.modules.business.service.IpLibraryService;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.modules.system.service.DetailedFileService;
import com.cumulus.utils.ExcelResolve;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RegexUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ip库服务实现
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class IpLibraryServiceImpl implements IpLibraryService {

    /**
     * 资产数据访问接口
     */
    @Resource
    private AssetRepository assetRepository;

    /**
     * ip库传输对象与ip库的映射
     */
    @Resource
    private IpLibraryMapper mapper;

    /**
     * ip库数据访问接口
     */
    @Resource
    private IpLibraryRepository repository;

    /**
     * 部门数据访问接口
     */
    @Resource
    private DeptRepository deptRepository;

    /**
     * 导出文件服务
     */
    @Resource
    private DetailedFileService detailedFileService;

    private static String object = "lock";

    /**
     * 查询ip库
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return ip列表
     */
    @Override
    public Object queryAll(IpLibraryQueryCriteria criteria, Pageable pageable) {
        Page<IpLibrary> ipLibraryPage = repository.findAll((root, query, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), pageable);
        return ipLibraryPage.map(mapper::toDto);
    }

    public static void main(String[] args) {

    }
    /**
     * 根据ip段添加
     *
     * @param ipLibraryDto ip库传输对象
     */
    @Override
    public void createByIpRange(IpLibraryDto ipLibraryDto) {
        String ipRange = RegexUtil.removeRNT(ipLibraryDto.getIpRange());
        DetectTaskIpDto detectTaskIpDto = DetectTaskServiceImpl.getDetectTaskIpDto(ipRange);
        List<String> list = DetectManager.ipParse(ipRange, detectTaskIpDto);
        Set<String> set = new HashSet<>(Collections.singletonList(ipRange));
        DetectTaskServiceImpl.isIp(set);
        Dept dept = ipLibraryDto.getDept();
        list.forEach(o -> {
            String completeIp = RegexUtil.checkToComplete(o, false);
            synchronized (object) {
                if (repository.countByCompleteIpEquals(completeIp) == 0) {
                    IpLibrary library = new IpLibrary();
                    library.setIp(o);
                    library.setCompleteIp(completeIp);
                    library.setDept(dept);
                    repository.save(library);
                }
            }
        });
    }

    /**
     * 批量新增
     *
     * @param file ip列表
     * @return 导入结果
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
        List<IpLibraryDto> ipLibraryDtoList = new ArrayList<>();
        if (jsonArray.isEmpty()) {
            log.warn("excel为空");
            return ipLibraryDtoList;
        }
        jsonArray.forEach(obj -> {
            Map map = objectMapper.convertValue(obj, Map.class);
            IpLibraryDto ipLibraryDto = new IpLibraryDto();
            ipLibraryDto.setIp(map.get("*资产IP").toString());
            ipLibraryDto.setDeptName(map.get("归属部门").toString());
            //根据名称查询部门
            List<Dept> deptList = deptRepository.findAllByName(ipLibraryDto.getDeptName());
            if (deptList.isEmpty()) {
                ipLibraryDto.setResult("部门不存在");
                fail.getAndIncrement();
            } else {
                ipLibraryDto.setDept(deptList.get(0));
                String completeIp = RegexUtil.checkToComplete(ipLibraryDto.getIp(), false);
                if (completeIp == null || "".equals(completeIp)) {
                    ipLibraryDto.setResult("ip格式不正确");
                    fail.getAndIncrement();
                } else if (repository.countByCompleteIpEquals(completeIp) == 0) {
                    IpLibrary ipLibrary = mapper.toEntity(ipLibraryDto);
                    ipLibrary.setCompleteIp(completeIp);
                    repository.save(ipLibrary);
                    ipLibraryDto.setResult("添加成功");
                    success.getAndIncrement();
                } else {
                    ipLibraryDto.setResult("IP已存在");
                    fail.getAndIncrement();
                }
            }
            ipLibraryDtoList.add(ipLibraryDto);
        });
        importResultDto.getResult().setSuccess(success.get());
        importResultDto.getResult().setFail(fail.get());
        importResultDto.setObjectList(ipLibraryDtoList);
        return importResultDto;
    }

    /**
     * 根据id修改
     *
     * @param ipLibraryDto IP库传输对象
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateById(IpLibraryDto ipLibraryDto) {
        repository.updateDept(ipLibraryDto.getDept().getId(), ipLibraryDto.getId());
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeById(Long id) {
        IpLibrary ipLibrary = repository.findById(id).orElse(null);
        if (ipLibrary == null) {
            throw new BadRequestException("删除失败，当前ip不存在");
        }
        if (assetRepository.queryAssetsByCompleteIpEquals(ipLibrary.getCompleteIp()).isEmpty()) {
            repository.deleteById(id);
        } else {
            throw new BadRequestException("删除失败，当前ip存在资产");
        }
    }

    /**
     * 批量删除
     *
     * @param ids
     * @param delAll
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeBatch(Set<Long> ids, Boolean delAll) {
        if (delAll) {
            if (assetRepository.findAll().isEmpty()) {
                repository.deleteAll();
            } else {
                throw new BadRequestException("删除失败，当前ip存在资产");
            }
        } else {
            ids.forEach(this::removeById);
        }
    }

    /**
     * 导出
     *
     * @param ids      id列表
     * @param all      是否全部
     * @param response 响应
     */
    @Override
    public void export(Set<Long> ids, Boolean all, HttpServletResponse response) {
        List<IpLibrary> ipLibraryList;
        if (all) {
            ipLibraryList = repository.findAll();
        } else {
            ipLibraryList = repository.findAllById(ids);
        }
        try (OutputStream os = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + "file.xls");
            List<List<String>> dataList = new ArrayList<>();
            ipLibraryList.forEach(ipLibrary -> {
                dataList.add(Arrays.asList(ipLibrary.getIp(),
                        ipLibrary.getDept() == null ? "" : ipLibrary.getDept().getName()));
            });
            String[] title = {"*资产IP", "归属部门"};
            HSSFWorkbook workbook = new HSSFWorkbook();
            FileUtils.exportExcel(workbook, 0, "sheet1", title, dataList, false);
            detailedFileService.saveDetailedFile(
                    DetailedFileTypeEnum.IPLIBRARY_RESULT, "ip库", FileConstant.EXCEL_SUFFIX_XLS, workbook);
            workbook.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
