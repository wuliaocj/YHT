package com.example.demo.service;

import com.example.demo.domain.Promotion;

import java.util.List;

public interface PromotionService {

    List<Promotion> listAll();

    Promotion save(Promotion promotion);

    void delete(Integer id);
}
