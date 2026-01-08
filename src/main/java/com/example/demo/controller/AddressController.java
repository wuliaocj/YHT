package com.example.demo.controller;

import com.example.demo.domain.Address;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AddressService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/list/{userId}")
    public HttpResult list(@PathVariable Integer userId) {
        List<Address> list = addressService.listByUser(userId);
        return HttpResult.ok(list);
    }

    @PostMapping("/save")
    public HttpResult save(@RequestBody Address address) {
        Address saved = addressService.save(address);
        return HttpResult.ok(saved);
    }

    @PostMapping("/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        addressService.delete(id);
        return HttpResult.ok();
    }
}


