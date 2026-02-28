package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    int insert(OperationLog log);

    List<OperationLog> selectByCondition(@Param("module") String module, 
                                         @Param("adminId") Integer adminId, 
                                         @Param("startTime") String startTime, 
                                         @Param("endTime") String endTime);

    List<OperationLog> selectLoginLogs(@Param("username") String username, 
                                       @Param("ip") String ip, 
                                       @Param("startTime") String startTime, 
                                       @Param("endTime") String endTime);

    /**
     * 分页查询操作日志
     * @param module 模块
     * @param adminId 管理员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 操作日志列表
     */
    List<OperationLog> selectByConditionWithPage(@Param("module") String module, 
                                               @Param("adminId") Integer adminId, 
                                               @Param("startTime") String startTime, 
                                               @Param("endTime") String endTime,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit);

    /**
     * 分页查询登录日志
     * @param username 用户名
     * @param ip IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 登录日志列表
     */
    List<OperationLog> selectLoginLogsWithPage(@Param("username") String username, 
                                              @Param("ip") String ip, 
                                              @Param("startTime") String startTime, 
                                              @Param("endTime") String endTime,
                                              @Param("offset") int offset,
                                              @Param("limit") int limit);

    /**
     * 查询操作日志总数
     * @param module 模块
     * @param adminId 管理员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志总数
     */
    long selectOperationLogCount(@Param("module") String module, 
                               @Param("adminId") Integer adminId, 
                               @Param("startTime") String startTime, 
                               @Param("endTime") String endTime);

    /**
     * 查询登录日志总数
     * @param username 用户名
     * @param ip IP地址
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 登录日志总数
     */
    long selectLoginLogCount(@Param("username") String username, 
                            @Param("ip") String ip, 
                            @Param("startTime") String startTime, 
                            @Param("endTime") String endTime);
}
