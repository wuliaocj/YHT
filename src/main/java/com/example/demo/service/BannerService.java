package com.example.demo.service;

import com.example.demo.domain.Banner;

import java.util.List;

public interface BannerService {

    List<Banner> listAll();

    Banner save(Banner banner);

    void delete(Integer id);
}
