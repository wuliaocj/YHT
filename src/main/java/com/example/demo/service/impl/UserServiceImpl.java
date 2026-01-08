package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional
    public User saveOrUpdateByOpenid(User user) {
        User db = userMapper.selectByOpenid(user.getOpenid());
        if (db == null) {
            userMapper.insert(user);
            return user;
        } else {
            user.setId(db.getId());
            userMapper.update(user);
            return userMapper.selectById(db.getId());
        }
    }
    @Override
    public User getUserByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    public User createUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname("微信用户" + openid.substring(openid.length() - 6)); // 默认昵称
        user.setAvatarUrl(""); // 默认头像为空
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user;
    }

    @Override
    public List<User> listAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    public void updateUserStatus(Integer userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(status);
        userMapper.update(user);
    }
}


