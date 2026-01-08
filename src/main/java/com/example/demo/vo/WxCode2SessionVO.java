package com.example.demo.vo;
import lombok.Data;

/**
 * 微信code2Session接口返回结果
 */
@Data
public class WxCode2SessionVO {
    /**
     * 用户openid
     */
    private String openid;

    /**
     * 会话密钥
     */
    private String session_key;

    /**
     * 错误码
     */
    private Integer errcode;

    /**
     * 错误信息
     */
    private String errmsg;
}
