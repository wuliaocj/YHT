package com.example.demo.service;

import com.example.demo.domain.User;

import java.util.List;

public interface UserService {

    User getByOpenid(String openid);

    User saveOrUpdateByOpenid(User user);

    User getUserByOpenid(String openid);

        /**
         * 创建新用户
         * @param openid openid
         * @return 新用户
         */
        User createUser(String openid);

    List<User> listAllUsers();

    void updateUserStatus(Integer userId, Integer status);
}


