//package com.example.demo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 关闭跨域保护（否则会和自定义 CORS 冲突）
//                .cors().and()
//                .csrf().disable() // 本地开发可关闭 CSRF，生产按需开启
//                // 放行 OPTIONS 预检请求
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
//                        // 放行其他需要的接口（比如登录、banner 接口）
//                        .requestMatchers("/api/admin/**").permitAll()
//                        .anyRequest().authenticated()
//                );
//        return http.build();
//    }
//}
