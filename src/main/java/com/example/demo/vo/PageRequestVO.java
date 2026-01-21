package com.example.demo.vo;

import lombok.Data;

/**
 * 分页查询请求VO
 */
@Data
public class PageRequestVO {
    
    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 排序字段
     */
    private String orderBy;
    
    /**
     * 排序方向（asc/desc）
     */
    private String orderDirection = "desc";
    
    /**
     * 获取偏移量
     * @return offset
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
    
    /**
     * 校验参数
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        if (orderDirection == null || (!"asc".equals(orderDirection) && !"desc".equals(orderDirection))) {
            orderDirection = "desc";
        }
    }
}