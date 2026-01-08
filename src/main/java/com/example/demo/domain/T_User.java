package com.example.demo.domain;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 用户表
 */
@Data

public class T_User {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 小程序openid
     */
    private String openid;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
