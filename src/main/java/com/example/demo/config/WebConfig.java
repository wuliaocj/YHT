package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /upload/images/** 路径到本地上传目录
        registry.addResourceHandler("/upload/images/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
