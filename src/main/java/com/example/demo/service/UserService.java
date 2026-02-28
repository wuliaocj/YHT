package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;

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

    User getById(Integer id);

    User updateUser(User user);

    /**
     * 分页获取用户列表
     * @param pageRequest 分页请求参数
     * @param keyword 搜索关键词（用户名或手机号）
     * @param status 用户状态
     * @return 分页响应
     */
    PageResponseVO<User> getUserListByPage(PageRequestVO pageRequest, String keyword, Integer status);
}


