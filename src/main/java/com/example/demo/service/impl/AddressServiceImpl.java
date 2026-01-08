package com.example.demo.service.impl;

import com.example.demo.domain.Address;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<Address> listByUser(Integer userId) {
        return addressMapper.selectByUserId(userId);
    }

    @Override
    public Address getById(Integer id) {
        return addressMapper.selectById(id);
    }

    @Override
    @Transactional
    public Address save(Address address) {
        if (address.getId() == null) {
            addressMapper.insert(address);
        } else {
            addressMapper.update(address);
        }
        return addressMapper.selectById(address.getId());
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        addressMapper.deleteById(id);
    }
}


