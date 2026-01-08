//package com.example.demo.config;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsFilter corsFilter() {
//        // 1. 创建 CORS 配置对象
//        CorsConfiguration config = new CorsConfiguration();
//        // 允许的源（这里允许前端 5173 端口访问，也可以用 "*" 允许所有源，生产环境不建议）
//        config.addAllowedOrigin("http://localhost:5173");
//        // 允许的请求头（* 表示所有）
//        config.addAllowedHeader("*");
//        // 允许的请求方法（* 表示所有：GET、POST、PUT、DELETE 等）
//        config.addAllowedMethod("*");
//        // 允许携带 Cookie（如果你的登录需要 Cookie/Token 认证，必须开启）
//        config.setAllowCredentials(true);
//        // 预检请求的缓存时间（秒），减少 OPTIONS 请求次数
//        config.setMaxAge(3600L);
//
//        // 2. 配置生效的 URL 路径（/** 表示所有接口）
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//
//        // 3. 返回 CORS 过滤器
//        return new CorsFilter(source);
//    }
//}
