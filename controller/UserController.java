package com.example.demo.controller;

import com.example.demo.domain.Result;
import com.example.demo.domain.User;
import com.example.demo.domain.VipLevel;
import com.example.demo.http.HttpResult;
import com.example.demo.service.UserService;
import com.example.demo.service.VipLevelService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.WxDataDecryptUtil;
import com.example.demo.util.WxLoginUtil;
import com.example.demo.vo.LoginResponseVO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import com.example.demo.vo.WxCode2SessionVO;
import com.alibaba.fastjson.JSONObject;
import cn.hutool.core.util.StrUtil;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final WxLoginUtil wxLoginUtil;
    private final WxDataDecryptUtil wxDataDecryptUtil;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final VipLevelService vipLevelService;

    /**
     * 登录请求参数（支持手机号和用户信息解密）
     */
    public static class LoginRequest {
        @NotBlank(message = "code不能为空")
        private String code;

        // 手机号加密数据（可选）
        private String encryptedData;
        // 解密向量（可选）
        private String iv;
        // 用户基本信息（可选，用于直接传递未加密的用户信息）
        private Map<String, Object> userInfo;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getEncryptedData() {
            return encryptedData;
        }

        public void setEncryptedData(String encryptedData) {
            this.encryptedData = encryptedData;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public Map<String, Object> getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(Map<String, Object> userInfo) {
            this.userInfo = userInfo;
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
     * @param request 请求参数（code, encryptedData, iv, userInfo）
     * @return 登录结果（token+openid+用户信息）
     */
    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        // 1. 调用微信接口获取openid和session_key
        WxCode2SessionVO wxResult = wxLoginUtil.getOpenidByCode(request.getCode());

        if (wxResult == null || (wxResult.getErrcode() != null && wxResult.getErrcode() != 0)) {
            log.warn("微信登录失败，errcode：{}，errmsg：{}",
                    wxResult != null ? wxResult.getErrcode() : "null",
                    wxResult != null ? wxResult.getErrmsg() : "接口调用失败");
            return Result.fail("微信登录失败：" + (wxResult != null ? wxResult.getErrmsg() : "接口调用失败"));
        }
        String openid = wxResult.getOpenid();
        String sessionKey = wxResult.getSession_key();
        if (openid == null || openid.isEmpty()) {
            log.warn("获取openid失败");
            return Result.fail("获取openid失败");
        }

        // 2. 解析手机号（如果有）
        String phoneNumber = null;
        if (StrUtil.isNotBlank(request.getEncryptedData()) && StrUtil.isNotBlank(request.getIv())) {
            phoneNumber = wxDataDecryptUtil.decryptPhoneNumber(
                    request.getEncryptedData(), request.getIv(), sessionKey);
            log.info("解密手机号成功：{}", phoneNumber != null ? "已获取" : "未获取");
        }

        // 3. 查询/创建用户
        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            user = userService.createUser(openid);
        }

        // 4. 检查用户状态（0=禁用，1=启用）
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用，无法登录，userId：{}，openid：{}", user.getId(), openid);
            return Result.fail("账号已被禁用");
        }

        // 6. 生成JWT token
        String token = jwtUtil.generateToken(openid, user.getId());
        log.info("用户登录成功，userId：{}，openid：{}", user.getId(), openid);

        // 7. 计算会员等级
        BigDecimal totalConsumption = user.getTotalConsumption();
        if (totalConsumption == null) {
            totalConsumption = BigDecimal.ZERO;
        }
        VipLevel currentLevel = vipLevelService.getVipLevelByAmount(totalConsumption);

        // 8. 构建响应结果
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(token);
        responseVO.setOpenid(openid);

        LoginResponseVO.UserInfoVO userInfoVO = new LoginResponseVO.UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatarUrl());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setTotalConsumption(user.getTotalConsumption());
        userInfoVO.setLevelName(currentLevel != null ? currentLevel.getName() : "普通会员");
        userInfoVO.setDiscountRate(currentLevel != null ? currentLevel.getDiscountRate() : BigDecimal.ONE);
        responseVO.setUserInfo(userInfoVO);

        log.info("用户登录成功，返回结果：{}", responseVO);
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
            // 设置测试用户信息（不设置头像，让前端显示默认头像）
            user.setNickname("测试用户");
            userService.updateUser(user);
        }

        // 4. 检查用户状态（0=禁用，1=启用）
        if (user.getStatus() != null && user.getStatus() == 0) {
            log.warn("用户已被禁用，无法登录，userId：{}，openid：{}", user.getId(), testOpenid);
            return Result.fail("账号已被禁用");
        }

        // 5. 生成JWT token
        String token = jwtUtil.generateToken(testOpenid, user.getId());
        log.info("测试账号登录成功，userId：{}，username：{}", user.getId(), request.getUsername());

        // 6. 计算会员等级
        BigDecimal totalConsumption = user.getTotalConsumption();
        if (totalConsumption == null) {
            totalConsumption = BigDecimal.ZERO;
        }
        VipLevel currentLevel = vipLevelService.getVipLevelByAmount(totalConsumption);

        // 7. 构建响应结果
        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(token);
        responseVO.setOpenid(testOpenid);

        LoginResponseVO.UserInfoVO userInfoVO = new LoginResponseVO.UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setNickname(user.getNickname());
        userInfoVO.setAvatar(user.getAvatarUrl());
        userInfoVO.setGender(user.getGender());
        userInfoVO.setPhone(user.getPhone());
        userInfoVO.setTotalConsumption(user.getTotalConsumption());
        userInfoVO.setLevelName(currentLevel != null ? currentLevel.getName() : "普通会员");
        userInfoVO.setDiscountRate(currentLevel != null ? currentLevel.getDiscountRate() : BigDecimal.ONE);
        responseVO.setUserInfo(userInfoVO);

        log.info("测试账号登录返回结果：{}", responseVO);

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
        log.info("检查token结果：{}", result);
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
        log.info("更新用户状态成功，userId：{}，status：{}", userId, status);
        return HttpResult.ok("更新成功");
    }

    /**
     * 管理员获取用户列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词（用户名或手机号）
     * @param status 用户状态
     * @return 分页用户列表
     */
    @GetMapping("/admin/user/list")
    public HttpResult adminListUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            PageResponseVO<User> pageResponse = userService.getUserListByPage(pageRequest, keyword, status);
            log.info("管理员查询用户列表，页码：{}，每页大小：{}，总记录数：{}",
                    pageRequest.getPageNum(), pageRequest.getPageSize(), pageResponse.getTotal());

            return HttpResult.ok(pageResponse);
        } catch (Exception e) {
            log.error("查询用户列表失败：", e);
            return HttpResult.error("查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/info")
    public HttpResult getUserInfo() {
        try {
            User user = getCurrentUser();
            if (user == null) {
                return HttpResult.error("用户未登录");
            }
            // 不返回敏感信息
            user.setOpenid(null);

            log.info("获取用户信息成功，userId：{}", user.getId());
            return HttpResult.ok(user);
        } catch (Exception e) {
            log.error("获取用户信息失败：", e);
            return HttpResult.error("获取用户信息失败");
        }
    }

    /**
     * 更新用户信息
     * @param updateRequest 更新请求参数
     * @return 更新结果
     */
    @PostMapping("/update")
    public HttpResult updateUserInfo(@RequestBody UserUpdateRequest updateRequest) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return HttpResult.error("用户未登录");
            }

            User user = userService.getById(currentUser.getId());
            if (user == null) {
                return HttpResult.error("用户不存在");
            }

            // 更新可修改的字段
            if (updateRequest.getNickname() != null) {
                user.setNickname(updateRequest.getNickname());
            }
            if (updateRequest.getAvatarUrl() != null) {
                user.setAvatarUrl(updateRequest.getAvatarUrl());
            }
            if (updateRequest.getGender() != null) {
                user.setGender(updateRequest.getGender());
            }
            if (updateRequest.getPhone() != null) {
                user.setPhone(updateRequest.getPhone());
            }

            userService.updateUser(user);
            log.info("用户信息更新成功，userId：{}", user.getId());
            return HttpResult.ok("更新成功");
        } catch (Exception e) {
            log.error("更新用户信息失败：", e);
            return HttpResult.error("更新用户信息失败");
        }
    }

    /**
     * 更新手机号
     * @param phone 新手机号
     * @return 更新结果
     */
    @PostMapping("/updatePhone")
    public HttpResult updatePhone(@RequestParam String phone) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return HttpResult.error("用户未登录");
            }

            User user = userService.getById(currentUser.getId());
            if (user == null) {
                return HttpResult.error("用户不存在");
            }

            user.setPhone(phone);
            userService.updateUser(user);
            log.info("用户手机号更新成功，userId：{}", user.getId());
            return HttpResult.ok("手机号更新成功");
        } catch (Exception e) {
            log.error("更新手机号失败：", e);
            return HttpResult.error("更新手机号失败");
        }
    }

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            String openid = (String) authentication.getPrincipal();
            if (openid == null) {
                return null;
            }
            log.info("获取当前用户成功，openid：{}", openid);
            return userService.getUserByOpenid(openid);
        } catch (Exception e) {
            log.warn("获取当前用户失败：", e);
            return null;
        }
    }

    /**
     * 用户信息更新请求参数
     */
    public static class UserUpdateRequest {
        private String nickname;
        private String avatarUrl;
        private Integer gender;
        private String province;
        private String city;
        private String phone;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public Integer getGender() {
            return gender;
        }

        public void setGender(Integer gender) {
            this.gender = gender;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
