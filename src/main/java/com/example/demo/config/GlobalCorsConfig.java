//package com.example.demo.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * 全局跨域配置（比 CorsFilter 更推荐的方式，适配 Spring Boot 2.x/3.x）
// */
//@Configuration
//public class GlobalCorsConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // 对所有接口生效
//        registry.addMapping("/**")
//                // 允许的前端源（本地开发是 5173，生产环境替换为实际域名）
//                .allowedOrigins("http://localhost:5173")
//                // 允许的请求方法（覆盖 POST/GET/OPTIONS 等）
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                // 允许的请求头（* 表示所有，比如 Token、Content-Type 等）
//                .allowedHeaders("*")
//                // 允许携带 Cookie/Token（登录态、认证需要）
//                .allowCredentials(true)
//                // 预检请求缓存时间（3600 秒，减少 OPTIONS 请求次数）
//                .maxAge(3600);
//    }
//}
