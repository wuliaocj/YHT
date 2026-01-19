package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    Banner selectById(@Param("id") Integer id);

    List<Banner> selectAll();

    int insert(Banner banner);

    int update(Banner banner);

    int delete(@Param("id") Integer id);
}
