package com.cumulus.modules.license.controller;

import com.cumulus.modules.license.service.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 授权控制层
 */
@RestController
@RequestMapping("/api/license")
public class LicenseController {

    /**
     * 授权服务接口
     */
    @Autowired
    private LicenseService licenseService;

    /**
     * 生成授权申请文件
     */
    @GetMapping("/generateApplication")
    public void generateApplication() {
        licenseService.generateApplication();
    }


    /**
     * 上传授权文件
     * @param multipartFile
     */
    @PostMapping
    @ResponseBody
    public void getLicense(@RequestParam("file") MultipartFile multipartFile){
        try {
            licenseService.getLicense(multipartFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
