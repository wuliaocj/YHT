package com.example.demo.service.impl;

import com.example.demo.domain.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User getByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User saveOrUpdateByOpenid(User user) {
        User db = userMapper.selectByOpenid(user.getOpenid());
        if (db == null) {
            userMapper.insert(user);
            log.info("创建新用户，openid：{}", user.getOpenid());
            return user;
        } else {
            user.setId(db.getId());
            userMapper.update(user);
            log.info("更新用户信息，userId：{}", db.getId());
            return userMapper.selectById(db.getId());
        }
    }
    @Override
    public User getUserByOpenid(String openid) {
        return userMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User createUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname("微信用户" + openid.substring(openid.length() - 6)); // 默认昵称
        user.setAvatarUrl(""); // 默认头像为空
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        log.info("创建新用户，userId：{}，openid：{}", user.getId(), openid);
        return user;
    }

    @Override
    public List<User> listAllUsers() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Integer userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        log.info("更新用户状态，userId：{}，status：{}", userId, status);
    }
}


