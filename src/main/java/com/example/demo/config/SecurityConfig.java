package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF（小程序无需）
                .csrf(csrf -> csrf.disable())
                // 关闭Session（JWT无状态）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置权限
                .authorizeHttpRequests(auth -> auth
                        // 公开接口
                        .antMatchers("/api/user/login", "/api/admin/login").permitAll()
                        .antMatchers("/api/payment/callback").permitAll()
                        .antMatchers("/api/product/list", "/api/product/detail").permitAll()
                        .antMatchers("/api/banner/list").permitAll()
                        .antMatchers("/api/category/list").permitAll()
                        .antMatchers("/api/order/admin/order/list").permitAll()//零时添加，后续找到方法再修改
                        .antMatchers("/upload/**").permitAll() // 允许公开访问上传的文件
                        .antMatchers("/error").permitAll()

                        // 管理员接口（需要ADMIN角色）
                        .antMatchers("/api/admin/**").hasRole("ADMIN")

                        // 用户接口（需要USER角色）
                        .antMatchers("/api/user/**").hasRole("USER")
                        .antMatchers("/api/order/**").hasRole("USER")
                        .antMatchers("/api/cart/**").hasRole("USER")
                        .antMatchers("/api/payment/create").hasRole("USER")

                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // 异常处理
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
