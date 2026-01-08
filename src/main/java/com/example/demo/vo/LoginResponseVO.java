package com.example.demo.vo;
import lombok.Data;

/**
 * 登录响应结果
 */
@Data
public class LoginResponseVO {
    /**
     * JWT token
     */
    private String token;

    /**
     * 用户openid
     */
    private String openid;

    /**
     * 用户信息
     */
    private UserInfoVO userInfo;

    // 内部类：用户信息
    @Data
    public static class UserInfoVO {
        private String nickname;
        private String avatar;
    }
}
