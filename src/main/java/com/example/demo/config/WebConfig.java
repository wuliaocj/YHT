package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射配置和跨域配置
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

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置跨域
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // 允许所有来源，生产环境应该限制具体域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*").
                allowCredentials(true)
                .maxAge(3600); // 预检请求的缓存时间
    }
}
