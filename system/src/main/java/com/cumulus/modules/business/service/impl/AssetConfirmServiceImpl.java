package com.cumulus.modules.business.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.common.DetectConstant;
import com.cumulus.modules.business.dto.AssetConfirmDto;
import com.cumulus.modules.business.dto.AssetConfirmQueryCriteria;
import com.cumulus.modules.business.dto.AssetDto;
import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.entity.AssetConfig;
import com.cumulus.modules.business.entity.AssetConfirm;
import com.cumulus.modules.business.entity.RemoteScan;
import com.cumulus.modules.business.mapstruct.AssetConfirmMapper;
import com.cumulus.modules.business.mapstruct.SimpleAssetConfirmMapper;
import com.cumulus.modules.business.repository.AssetConfirmRepository;
import com.cumulus.modules.business.service.AssetConfirmService;
import com.cumulus.modules.business.service.AssetService;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.QueryUtils;
import com.cumulus.utils.RegexUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 确认资产服务实现类
 *
 * @author zhangxq
 */
@Slf4j
@Service
public class AssetConfirmServiceImpl implements AssetConfirmService {

    /**
     * 确认资产传输对象与确认资产实体对象的映射
     */
    @Resource
    private AssetConfirmMapper mapper;

    /**
     * 确认资产数据访问接口
     */
    @Resource
    private AssetConfirmRepository repository;

    /**
     * 简单确认资产数据传输对象
     */
    @Resource
    private SimpleAssetConfirmMapper simpleAssetConfirmMapper;

    /**
     * 资产服务
     */
    @Resource
    private AssetService assetService;

    /**
     * 允许的登录协议
     */
    private static final String[] PROTOCOLS = {"ssh", "SSH", "Telnet", "telnet", "Winrm", "winrm", "无访问"};

    /**
     * 采集方式
     */
    private static final String[] GATHERTYPE = {"", "agent", "登录采集-", "无访问"};

    /**
     * 查询确认资产
     *
     * @param criteria 查询参数
     * @param pageable 分页参数
     * @return 资产列表
     */
    @Override
    public Object queryAll(AssetConfirmQueryCriteria criteria, Pageable pageable) {
        Page<AssetConfirm> assetConfirmPage = repository.findAll((root, query, criteriaBuilder) ->
                QueryUtils.getPredicate(root, criteria, criteriaBuilder), pageable);
        return assetConfirmPage.map(mapper::toDto);
    }

    /**
     * 根据id修改
     *
     * @param assetConfirmDto 确认资产传输对象
     */
    @Override
    public void updateById(AssetConfirmDto assetConfirmDto) {
        repository.save(mapper.toEntity(assetConfirmDto));
    }

    /**
     * 根据id删除
     *
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeById(Long id) {
        AssetConfirm byId = repository.getById(id);
        repository.deleteByCompleteIp(byId.getCompleteIp());
    }

    /**
     * 批量删除
     *
     * @param ids    id
     * @param delAll 是否删除全部
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatch(Set<Long> ids, boolean delAll) {
        if (delAll) {
            repository.deleteAll();
        } else {
            for (Long id : ids) {
                AssetConfirm byId = repository.getById(id);
                repository.deleteByCompleteIp(byId.getCompleteIp());
            }
        }

    }

    /**
     * 导出excel
     *
     * @param ids id列表
     * @param all 是否全部
     */
    @Override
    public void exportZip(Set<Long> ids, boolean all, HttpServletResponse response, HttpServletRequest request) {
        List<AssetConfirm> assetConfirmList;
        if (all) {
            assetConfirmList = repository.findAll();
        } else {
            assetConfirmList = repository.findAllById(ids);
        }
        //区分主机和应用
        List<AssetConfirm> hostList =
                assetConfirmList.stream().filter(e -> e.getAssetCategory() == 1).collect(Collectors.toList());
        List<AssetConfirm> webList =
                assetConfirmList.stream().filter(e -> e.getAssetCategory() == 2).collect(Collectors.toList());
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=fileTemplate.zip");
        try (OutputStream os = response.getOutputStream()) {
            String path = request.getSession().getServletContext().getRealPath("/template/excel");
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String zipPath = path + "/temp.zip";
            File zip = new File(zipPath);
            File hostFile = exportHost(hostList, path);
            File webFile = exportWeb(webList, path);
            File srcFile[] = {hostFile, webFile};
            ZipFiles(srcFile, zip);
            InputStream inputStream = new FileInputStream(zipPath);
            byte[] buffer = new byte[1024];
            int i = -1;
            while ((i = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, i);
            }
            os.flush();
            os.close();
            inputStream.close();
            delAllFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 导出excel
     *
     * @param ids      id列表
     * @param all      是否全部
     * @param response
     */
    @Override
    public void exportExcel(Set<Long> ids, boolean all, HttpServletResponse response) {
        List<AssetConfirm> hostList;
        if (all) {
            hostList = repository.findAll();
        } else {
            hostList = repository.findAllById(ids);
        }
        //只导出主机
        hostList = hostList.stream().filter(e -> e.getAssetCategory() == 1).collect(Collectors.toList());
        List<List<String>> dataList = new ArrayList<>();
        String[] title = {"*资产IP", "*资产名称", "*资产类型", "*采集方式", "*登录端口", "*登录账户",
                "*登录密码", "资产标签", "资产归属"};
        try (OutputStream os = response.getOutputStream()) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=" + "file.xls");
            hostList.forEach(host -> {
                String typeName = host.getAssetType().getName();
                if (host.getAssetSysType() != null) {
                    typeName = host.getAssetSysType().getName();
                }
                dataList.add(new ArrayList<>(Arrays.asList(
                        host.getIp(),
                        "",
                        typeName,
                        GATHERTYPE[host.getGatherType()],
                        "",
                        "",
                        "",
                        "",
                        ""
                )));
            });
            HSSFWorkbook workbook = new HSSFWorkbook();
            FileUtils.exportExcel(workbook, 0, "sheet1", title, dataList, false);
            workbook.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据ip查找端口资产
     *
     * @param ip 全写ip
     * @return 端口信息列表
     */
    @Override
    public Object getByIp(Pageable pageable, String ip) {
        Page<AssetConfirm> assetConfirmList = repository.queryByCompleteIpAndAssetCategoryEquals(
                pageable, RegexUtil.checkToComplete(ip, true), 2);
        return assetConfirmList.map(simpleAssetConfirmMapper::toDto);
    }

    /**
     * 单个确认
     *
     * @param id
     */
    @Override
    public void singleConfirm(Long id) {
        batchConfirm(Collections.singletonList(id), false);
    }

    /**
     * 批量确认
     *
     * @param ids
     * @param isAll
     */
    @Override
    public void batchConfirm(Collection<Long> ids, boolean isAll) {
        List<AssetConfirm> confirmList;
        if (isAll) {
            confirmList = repository.queryByAssetCategoryEquals(Asset.CATEGORY_HOST);
        } else {
            confirmList = repository.findAllById(ids);
        }
        if (confirmList.isEmpty()) {
            throw new BadRequestException("选中资产为空");
        }
        confirmList.forEach(e -> {
            if (e.getDept() == null || e.getGatherType() == null) {
                throw new BadRequestException(e.getIp() + "未补全信息");
            }
        });
        addAsset(confirmList);
    }

    /**
     * 添加到资产清单
     *
     * @param confirmList
     */
    private void addAsset(List<AssetConfirm> confirmList) {
        confirmList.forEach(e -> {
            AssetDto assetDto = new AssetDto();
            assetDto.setAssetCategory(Asset.CATEGORY_HOST);
            assetDto.setIp(e.getIp());
            assetDto.setDept(e.getDept());
            AssetConfig config = new AssetConfig();
            config.setGatherType(Collections.singletonList(e.getGatherType()));
            //如果为远程扫描则添加 RemoteScan
            if (e.getGatherType() == DetectConstant.GATHER_TYPE_SCAN) {
                config.setRemoteScan(new RemoteScan());
            }
            assetDto.setAssetConfig(config);
            assetService.createByAssetConfirm(assetDto, false);
        });
    }

    /**
     * 导出主机excel
     *
     * @param hostList 主机确认资产列表
     * @return 文件
     */
    private File exportHost(List<AssetConfirm> hostList, String path) throws IOException {
        List<List<String>> dataList = new ArrayList<>();
        String[] title = {"*资产IP", "*资产类型", "*登录端口", "*资产名称", "*登录协议", "*账户", "*密码",
                "归属部门", "资产标签"};
        hostList.forEach(host ->
                dataList.add(new ArrayList<>(Arrays.asList(
                        host.getIp(), "", "", "", "", "", "", "", ""
                )))
        );
        HSSFWorkbook workbook = new HSSFWorkbook();
        FileUtils.exportExcel(workbook, 0, "sheet1", title, dataList, false);
        String filePath = path + "/主机资产导入模板.xls";
        File file = new File(filePath);
        workbook.write(file);
        return file;
    }

    /**
     * 导出主机excel
     *
     * @param webList 应用确认资产列表
     * @return 文件
     */
    private File exportWeb(List<AssetConfirm> webList, String path) throws IOException {
        List<List<String>> dataList = new ArrayList<>();
        String[] title =
                {"*资产IP", "*资产类型", "*开放端口", "*资产名称", "*服务", "*网址", "*登录协议", "*账户", "*密码", "归属部门", "资产标签"};
        List<String> protocols = Arrays.asList(PROTOCOLS);
        webList.forEach(web ->
                dataList.add(new ArrayList<>(Arrays.asList(
                        web.getIp(),
                        "",
                        web.getPort().toString(),
                        "",
                        protocols.contains(web.getServer()) ? web.getServer() : "",
                        web.getWebsite(),
                        web.getProtocol(),
                        "", "", "", ""
                )))
        );
        HSSFWorkbook workbook = new HSSFWorkbook();
        FileUtils.exportExcel(workbook, 0, "sheet1", title, dataList, false);
        String filePath = path + "/应用资产导入模板.xls";
        File file = new File(filePath);
        workbook.write(file);
        return file;
    }

    /**
     * 压缩文件
     *
     * @param srcfile 源文件
     * @param zipfile 目标文件
     */
    public void ZipFiles(File[] srcfile, File zipfile) {
        byte[] buf = new byte[1024];
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (int i = 0; i < srcfile.length; i++) {
                FileInputStream in = new FileInputStream(srcfile[i]);
                out.putNextEntry(new ZipEntry(srcfile[i].getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     * @return
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
                flag = true;
            }
        }
        return flag;
    }


}
