package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.example.demo.domain.Result;
import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.WxLoginUtil;
import com.example.demo.vo.LoginResponseVO;
import com.example.demo.vo.WxCode2SessionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private WxLoginUtil wxLoginUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 小程序登录 / 注册：根据 openid 同步用户基础信息
     */
//    @PostMapping("/savelogin")
//    public HttpResult login(@RequestBody User user) {
//        if (user.getOpenid() == null) {
//            return HttpResult.error("openid不能为空");
//        }
//        User saved = userService.saveOrUpdateByOpenid(user);
//        return HttpResult.ok(saved);
//    }

        /**
         * 登录请求参数
         */
        public static class LoginRequest {
            @NotBlank(message = "code不能为空")
            private String code;

            public String getCode() {
                return code;
            }

            public void setCode(String code) {
                this.code = code;
            }
        }
        /**
         * 微信小程序登录接口
         * @param request 请求参数（code）
         * @return 登录结果（token+openid+用户信息）
         */
        @PostMapping("/login")
        public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
            // 1. 调用微信接口获取openid
            WxCode2SessionVO wxResult = wxLoginUtil.getOpenidByCode(request.getCode());

            if (wxResult == null || wxResult.getErrcode() != null && wxResult.getErrcode() != 0) {
                return Result.fail("微信登录失败：" + (wxResult != null ? wxResult.getErrmsg() : "接口调用失败"));
            }
            String openid = wxResult.getOpenid();
            if (openid == null || openid.isEmpty()) {
                return Result.fail("获取openid失败");
            }
            // 2. 查询/创建用户
            User user = userService.getUserByOpenid(openid);
            if (user == null) {
                user = userService.createUser(openid);
            }
            // 3. 生成JWT token
            String token = jwtUtil.generateToken(openid);
            // 4. 构建响应结果
            LoginResponseVO responseVO = new LoginResponseVO();
            responseVO.setToken(token);
            responseVO.setOpenid(openid);

            LoginResponseVO.UserInfoVO userInfoVO = new LoginResponseVO.UserInfoVO();
            userInfoVO.setNickname(user.getNickname());
            userInfoVO.setAvatar(user.getAvatarUrl());
            responseVO.setUserInfo(userInfoVO);
            return Result.success(responseVO);
        }

    // 新增：校验 token 有效性接口
    @GetMapping("/checkToken")
    public Map<String, Object> checkToken() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Spring Security 自动校验 token，若无效会直接返回 401
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                result.put("code", 200);
                result.put("msg", "token 有效");
                result.put("data", null);
            } else {
                result.put("code", 401);
                result.put("msg", "token 无效");
            }
        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "token 过期/无效");
        }
        return result;
    }

    @PostMapping("/admin/user/updateStatus/{userId}")
    public HttpResult adminUpdateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        userService.updateUserStatus(userId, status);
        return HttpResult.ok("更新成功");
    }
}


