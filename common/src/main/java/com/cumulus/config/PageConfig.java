package com.cumulus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author : shenjc
 *
 * 分页参数设置
 */
@Configuration
public class PageConfig implements WebMvcConfigurer {

    /**
     * 单页最大数据量
     */
    @Value("${page-size-maximum}")
    private Integer pageSizeMaximum;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setMaxPageSize(pageSizeMaximum);
        argumentResolvers.add(resolver);
    }
}