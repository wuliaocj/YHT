package com.example.demo.service;

import com.example.demo.domain.Address;

import java.util.List;

public interface AddressService {

    List<Address> listByUser(Integer userId);

    Address getById(Integer id);

    Address save(Address address);

    void delete(Integer id);
}


