package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.domain.Promotion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PromotionMapper extends BaseMapper<Promotion> {

    Promotion selectById(@Param("id") Integer id);

    List<Promotion> selectAll();

    int insert(Promotion promotion);

    int update(Promotion promotion);

    int delete(@Param("id") Integer id);
}
