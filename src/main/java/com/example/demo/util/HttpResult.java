package com.example.demo.util;

/**
 * HTTP响应结果类
 * 用于统一返回结果格式
 */
public class HttpResult {

    /**
     * 状态码：200=成功，500=失败
     */
    private int code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private Object data;

    /**
     * 构造方法
     * @param code 状态码
     * @param msg 消息
     * @param data 数据
     */
    private HttpResult(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 成功响应
     * @return 成功响应对象
     */
    public static HttpResult ok() {
        return new HttpResult(200, "操作成功", null);
    }

    /**
     * 成功响应
     * @param msg 成功消息
     * @return 成功响应对象
     */
    public static HttpResult ok(String msg) {
        return new HttpResult(200, msg, null);
    }

    /**
     * 成功响应
     * @param msg 成功消息
     * @param data 数据
     * @return 成功响应对象
     */
    public static HttpResult ok(String msg, Object data) {
        return new HttpResult(200, msg, data);
    }

    /**
     * 错误响应
     * @param msg 错误消息
     * @return 错误响应对象
     */
    public static HttpResult error(String msg) {
        return new HttpResult(500, msg, null);
    }

    /**
     * 错误响应
     * @param code 错误码
     * @param msg 错误消息
     * @return 错误响应对象
     */
    public static HttpResult error(int code, String msg) {
        return new HttpResult(code, msg, null);
    }

    // Getter and Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
