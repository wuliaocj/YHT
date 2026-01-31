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
}
