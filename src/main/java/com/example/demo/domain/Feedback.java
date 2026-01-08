package com.example.demo.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户反馈表
 */
@Data
public class Feedback {

    private Integer id;
    private Integer userId;
    private Integer type;
    private String content;
    private String contact;
    private String images;
    private Integer status;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
}


