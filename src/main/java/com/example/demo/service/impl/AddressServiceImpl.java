package com.example.demo.service.impl;

import com.example.demo.domain.Address;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    @Override
    public List<Address> listByUser(Integer userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getById(Integer id) {
        return addressMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address save(Address address) {
        if (address.getId() == null) {
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());
            addressMapper.insert(address);
            log.info("新增地址成功，addressId：{}", address.getId());
        } else {
            address.setUpdateTime(LocalDateTime.now());
            addressMapper.update(address);
            log.info("更新地址成功，addressId：{}", address.getId());
        }
        return addressMapper.selectById(address.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        addressMapper.deleteById(id);
        log.info("删除地址成功，addressId：{}", id);
    }
}


