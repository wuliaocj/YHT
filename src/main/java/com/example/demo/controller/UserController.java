package com.example.demo.controller;

import com.example.demo.domain.Result;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.WxLoginUtil;
import com.example.demo.vo.LoginResponseVO;
import com.example.demo.vo.WxCode2SessionVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final WxLoginUtil wxLoginUtil;
    private final UserService userService;
    private final JwtUtil jwtUtil;

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
     * 测试账号登录请求参数
     */
    public static class TestLoginRequest {
        @NotBlank(message = "username不能为空")
        private String username;
        @NotBlank(message = "password不能为空")
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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

        if (wxResult == null || (wxResult.getErrcode() != null && wxResult.getErrcode() != 0)) {
            log.warn("微信登录失败，errcode：{}，errmsg：{}",
                    wxResult != null ? wxResult.getErrcode() : "null",
                    wxResult != null ? wxResult.getErrmsg() : "接口调用失败");
            return Result.fail("微信登录失败：" + (wxResult != null ? wxResult.getErrmsg() : "接口调用失败"));
        }
        String openid = wxResult.getOpenid();
        if (openid == null || openid.isEmpty()) {
            log.warn("获取openid失败");
            return Result.fail("获取openid失败");
        }
        // 2. 查询/创建用户
        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            user = userService.createUser(openid);
        }
        // 3. 生成JWT token
        String token = jwtUtil.generateToken(openid);
        log.info("用户登录成功，userId：{}，openid：{}", user.getId(), openid);
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

    /**
     * 测试账号登录接口
     * @param request 请求参数（username, password）
     * @return 登录结果（token+用户信息）
     */
    @PostMapping("/test/login")
    public Result<LoginResponseVO> testLogin(@Valid @RequestBody TestLoginRequest request) {
        // 1. 验证测试账号
        if (!"test".equals(request.getUsername()) || !"123456".equals(request.getPassword())) {
            log.warn("测试账号登录失败，username：{}", request.getUsername());
            return Result.fail("用户名或密码错误");
        }
        
        // 2. 使用固定的测试openid
        String testOpenid = "test_openid_123456";
        
        // 3. 查询/创建测试用户
        User user = userService.getUserByOpenid(testOpenid);
        if (user == null) {
            user = userService.createUser(testOpenid);
            // 设置测试用户信息
            user.setNickname("测试用户");
            user.setAvatarUrl("https://via.placeholder.com/100");
            userService.updateUser(user);
        }
        
        // 4. 生成JWT token
        String token = jwtUtil.generateToken(testOpenid);
        log.info("测试账号登录成功，userId：{}，username：{}", user.getId(), request.getUsername());
        
        // 5. 构建响应结果
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(token);
        responseVO.setOpenid(testOpenid);

        LoginResponseVO.UserInfoVO userInfoVO = new LoginResponseVO.UserInfoVO();
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatarUrl());
        responseVO.setUserInfo(userInfoVO);
        return Result.success(responseVO);
    }

    /**
     * 检查token有效性
     * @return token校验结果
     */
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
            log.warn("token校验异常：", e);
            result.put("code", 401);
            result.put("msg", "token 过期/无效");
        }
        return result;
    }

    /**
     * 管理员更新用户状态
     * @param userId 用户ID
     * @param request 请求参数（包含status）
     * @return 更新结果
     */
    @PostMapping("/admin/user/updateStatus/{userId}")
    public HttpResult adminUpdateUserStatus(@PathVariable Integer userId, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        if (status == null) {
            return HttpResult.error("状态不能为空");
        }
        userService.updateUserStatus(userId, status);
        return HttpResult.ok("更新成功");
    }
}
