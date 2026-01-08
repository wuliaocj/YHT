package com.example.demo.mapper;

import com.example.demo.domain.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {

    Address selectById(@Param("id") Integer id);

    List<Address> selectByUserId(@Param("userId") Integer userId);

    Address selectDefaultByUserId(@Param("userId") Integer userId);

    int insert(Address address);

    int update(Address address);

    int deleteById(@Param("id") Integer id);
}


