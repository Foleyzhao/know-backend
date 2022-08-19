package com.cumulus.modules.system.controller;

import com.cumulus.config.FileProperties;
import com.cumulus.constant.FileConstant;
import com.cumulus.enums.DetailedFileTypeEnum;
import com.cumulus.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件控制层
 *
 * @author : shenjc
 */
@Slf4j
@RestController
@RequestMapping("/api/file")
public class FileController {

    /**
     * 系统文件属性配置
     */
    @Autowired
    private FileProperties fileProperties;

    /**
     * 初始化文件夹
     */
    @PostConstruct
    public void init() {
        if (log.isInfoEnabled()) {
            log.info("init create folder");
        }
        String filePath = fileProperties.getPath().getPath();
        File templateFileFolder = new File(filePath + FileConstant.TEMPLATE_FILE_FOLDER);
        if (!templateFileFolder.exists() || !templateFileFolder.isDirectory()) {
            if (log.isErrorEnabled()) {
                log.error("file init error templateFileFolder not exit");
            }
        } else {
            for (FileConstant.TemplateFile templateFile : FileConstant.TemplateFile.values()) {
                String fileName = templateFile.getFileName() + templateFile.getFileSuffix();
                File file = new File(templateFileFolder.getPath() + FileConstant.FILE_SEPARATE
                        + fileName);
                if (!file.exists() && log.isErrorEnabled()) {
                    log.error("file init error file not exit name:{}", fileName);
                }
            }
        }
        List<File> fileList = new ArrayList<>();
        fileList.add(new File(filePath + FileConstant.FILE_SEPARATE + FileConstant.LOG_FILE_FOLDER));
        fileList.add(new File(filePath + FileConstant.FILE_SEPARATE + FileConstant.DETAILED_FILE_FOLDER));
        fileList.add(new File(filePath + FileConstant.FILE_SEPARATE + FileConstant.SCAN_RESULT_REPORT_FOLDER));
        for (DetailedFileTypeEnum folder : DetailedFileTypeEnum.values()) {
            fileList.add(new File(filePath + FileConstant.FILE_SEPARATE + folder.getFolder()));
        }
        for (File file : fileList) {
            if (file.exists()) {
                if (file.isDirectory()) {
                    continue;
                } else {
                    file.deleteOnExit();
                }
            }
            boolean mkdirs = file.mkdirs();
        }
    }

    /**
     * 下载模板文件
     *
     * @param response 请求响应
     * @param request  请求
     * @param fileName 文件名
     * @return 字典详情列表
     */
    @GetMapping("downloadTemplateFile")
    public ResponseEntity<?> downloadTemplateFile(String fileName, HttpServletRequest request, HttpServletResponse response) {
        FileConstant.TemplateFile templateFile = FileConstant.TemplateFile.valueOf(fileName);
        String path = fileProperties.getPath().getPath() +
                FileConstant.FILE_SEPARATE +
                FileConstant.TEMPLATE_FILE_FOLDER +
                FileConstant.FILE_SEPARATE +
                templateFile.getFileName() +
                templateFile.getFileSuffix();
        FileUtils.downloadFile(new File(path), response, request,
                templateFile.getFileNameChinese() + templateFile.getFileSuffix());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
