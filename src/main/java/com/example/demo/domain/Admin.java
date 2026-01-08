package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员表
 */
@Data
public class Admin {

    private Integer id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private Integer role;
    private Integer status;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


