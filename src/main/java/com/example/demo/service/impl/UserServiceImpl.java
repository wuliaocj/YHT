package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.domain.User;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.UserService;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
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

    @Override
    public User getById(Integer id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        log.info("更新用户信息，userId：{}", user.getId());
        return userMapper.selectById(user.getId());
    }

    @Override
    public PageResponseVO<User> getUserListByPage(PageRequestVO pageRequest, String keyword, Integer status) {
        // 校验分页参数
        pageRequest.validate();

        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加过滤条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> {
                wrapper.like(User::getNickname, keyword)
                       .or()
                       .like(User::getPhone, keyword);
            });
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 添加排序条件
        if (pageRequest.getOrderBy() != null && !pageRequest.getOrderBy().isEmpty()) {
            switch (pageRequest.getOrderBy()) {
                case "createTime":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), User::getCreateTime);
                    break;
                case "nickname":
                    queryWrapper.orderBy(true, "desc".equals(pageRequest.getOrderDirection()), User::getNickname);
                    break;
                default:
                    queryWrapper.orderByDesc(User::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间倒序
            queryWrapper.orderByDesc(User::getCreateTime);
        }
        
        // 计算总数
        long total = userMapper.selectCount(queryWrapper);
        if (total == 0) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        // 分页查询
        int offset = pageRequest.getOffset();
        int limit = pageRequest.getPageSize();
        List<User> records = userMapper.selectByPage(offset, limit, queryWrapper);
        
        if (records == null || records.isEmpty()) {
            return PageResponseVO.empty(pageRequest.getPageNum(), pageRequest.getPageSize());
        }
        
        return PageResponseVO.of(records, total, pageRequest.getPageNum(), pageRequest.getPageSize());
    }
}


