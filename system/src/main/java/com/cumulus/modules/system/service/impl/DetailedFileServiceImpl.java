package com.cumulus.modules.system.service.impl;

import com.cumulus.config.FileProperties;
import com.cumulus.constant.FileConstant;
import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.system.entity.DetailedFile;
import com.cumulus.modules.system.repository.DetailedFileRepository;
import com.cumulus.modules.system.service.DetailedFileService;
import com.cumulus.mysql.utils.QueryableIncludeOrSpecification;
import com.cumulus.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.POIDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * 明细清单服务接口实现类
 *
 * @author : shenjc
 */
@Slf4j
@Service
public class DetailedFileServiceImpl implements DetailedFileService {

    /**
     * 系统文件属性配置
     */
    @Autowired
    private FileProperties fileProperties;

    /**
     * 明细清单数据接口
     */
    @Autowired
    private DetailedFileRepository detailedFileRepository;

    @Override
    public Page<DetailedFile> page(Pageable pageable, MultiValueMap<String, String> params) {
        return detailedFileRepository.findAll(
                new QueryableIncludeOrSpecification<>(DetailedFile.class, params), pageable);
    }

    @Override
    public void deleteById(Long id) {
        Optional<DetailedFile> detailedFileOpt = detailedFileRepository.findById(id);
        if (!detailedFileOpt.isPresent()) {
            throw new BadRequestException("删除失败 记录不存在");
        }
        try {
            String path = getDetailedFilePathById(id);
            File file = new File(path);
            if (file.exists()) {
                boolean delete = file.delete();
            }
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("DetailedFileServiceImpl deleteById file delete error");
            }
        }
        detailedFileRepository.deleteById(id);
    }

    @Override
    public String deleteBatches(List<Long> list) {
        int error = 0;
        for (Long id : list) {
            try {
                deleteById(id);
            } catch (Exception e) {
                error++;
                if (log.isInfoEnabled()) {
                    log.info("DetailedFileServiceImpl deleteBatches file delete error");
                }
            }
        }
        int success = list.size() - error;
        return "删除成功,共" + list.size() + "条任务,成功删除" + success + "条";
    }

    @Override
    public String getDetailedFilePathById(Long id) {
        Optional<DetailedFile> detailedFileOpt = detailedFileRepository.findById(id);
        if (!detailedFileOpt.isPresent()) {
            throw new BadRequestException("删除失败 记录不存在");
        }
        DetailedFile detailedFile = detailedFileOpt.get();
        DetailedFileTypeEnum type = DetailedFileTypeEnum.getByType(detailedFile.getType());
        if (type == null) {
            throw new BadRequestException("文件不支持");
        }
        return fileProperties.getPath().getPath() +
                FileConstant.FILE_SEPARATE +
                type.getFolder() +
                FileConstant.FILE_SEPARATE +
                detailedFile.getFileName() + detailedFile.getFileSuffix();
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void saveDetailedFile(DetailedFileTypeEnum typeEnum, String name, String fileSuffix, POIDocument workbook) throws IOException {
        DetailedFile detailedFile = saveDetailedFile(typeEnum, name, fileSuffix);
        FileUtils.saveFile(workbook, getDetailedFilePathById(detailedFile.getId()));
        updateDetailedFileDone(detailedFile.getId());
    }

    @Override
    public DetailedFile saveDetailedFile(DetailedFileTypeEnum typeEnum, String name, String fileSuffix) {
        DetailedFile detailedFile = new DetailedFile();
        detailedFile.setName(name);
        detailedFile.setType(typeEnum.getType());
        detailedFile.setFileSuffix(fileSuffix);
        detailedFile.setFileName(String.valueOf(System.currentTimeMillis()));
        detailedFile.setStatus(DetailedFile.STATUS_GENERATING);
        detailedFileRepository.save(detailedFile);
        return detailedFile;
    }

    @Override
    public void updateDetailedFileDone(Long id) {
        Optional<DetailedFile> detailedFileOpt = detailedFileRepository.findById(id);
        if (!detailedFileOpt.isPresent()) {
            return;
        }
        detailedFileOpt.get().setStatus(DetailedFile.STATUS_DONE);
        detailedFileRepository.save(detailedFileOpt.get());
    }
}
