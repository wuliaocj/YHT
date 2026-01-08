package com.example.demo.service.impl;

import com.example.demo.domain.Banner;
import com.example.demo.mapper.BannerMapper;
import com.example.demo.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    private final BannerMapper bannerMapper;

    public BannerServiceImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @Override
    public List<Banner> listAll() {
        return bannerMapper.selectAll();
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.update(banner);
        }
        return banner;
    }

    @Override
    public void delete(Integer id) {
        bannerMapper.delete(id);
    }
}
