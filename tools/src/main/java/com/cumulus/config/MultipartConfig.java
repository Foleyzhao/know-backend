package com.cumulus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * 自定义Multipart配置
 *
 * @author zhaoff
 */
@Slf4j
@Configuration
public class MultipartConfig {

    /**
     * 修改临时文件的存储路径
     *
     * @return Multipart配置
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String location = System.getProperty("user.home") + "/.platform/file/tmp";
        File tmpFile = new File(location);
        if (!tmpFile.exists()) {
            if (!tmpFile.mkdirs()) {
                if (log.isErrorEnabled()) {
                    log.error("create temp dir was not successful.");
                }
            }
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }
}
