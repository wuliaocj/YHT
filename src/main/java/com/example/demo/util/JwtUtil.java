package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {
    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * token有效期（秒）
     */
    @Value("${jwt.expire}")
    private Long expire;

    /**
     * 生成token
     * @param openid 用户openid
     * @return token
     */
    public String generateToken(String openid) {
        // 构建密钥
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // 生成token
        return Jwts.builder()
                .setSubject(openid) // 存储openid
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expire * 1000)) // 过期时间
                .signWith(key) // 签名
                .compact();
    }

    /**
     * 解析token，获取openid
     * @param token token
     * @return openid
     */
    public String getOpenidFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 验证token是否有效
     * @param token token
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
