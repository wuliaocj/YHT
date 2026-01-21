package com.example.demo.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页查询响应VO
 */
@Data
public class PageResponseVO<T> {
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总页数
     */
    private Integer pages;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    /**
     * 构造函数
     */
    public PageResponseVO() {}
    
    /**
     * 构造函数
     * @param records 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public PageResponseVO(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = pageNum < pages;
        this.hasPrevious = pageNum > 1;
    }
    
    /**
     * 创建分页响应
     * @param records 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 分页响应对象
     */
    public static <T> PageResponseVO<T> of(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        return new PageResponseVO<>(records, total, pageNum, pageSize);
    }
    
    /**
     * 创建空分页响应
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @return 空分页响应对象
     */
    public static <T> PageResponseVO<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResponseVO<>(null, 0L, pageNum, pageSize);
    }
}