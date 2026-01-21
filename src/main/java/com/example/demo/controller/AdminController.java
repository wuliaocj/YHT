package com.example.demo.controller;

import com.example.demo.domain.Admin;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AdminService;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OrderService orderService;

    /**
     * 管理员登录
     * @param request 登录请求（包含username和password）
     * @return 登录结果（token和admin信息）
     */
    @PostMapping("/login")
    public HttpResult login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            log.warn("管理员登录失败：用户名或密码为空");
            return HttpResult.error("用户名和密码不能为空");
        }

        try {
            Admin admin = adminService.login(username, password);
            // 生成token（使用adminId作为subject）
            String token = jwtUtil.generateToken("admin_" + admin.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", admin);
            log.info("管理员登录成功，adminId：{}，username：{}", admin.getId(), username);
            return HttpResult.ok(result);
        } catch (RuntimeException e) {
            log.warn("管理员登录失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        }
    }

    /**
     * 获取当前管理员信息
     * @return 当前管理员信息
     */
    @GetMapping("/current")
    public HttpResult getCurrent() {
        // 这里应该从token中获取adminId，暂时返回示例
        // 实际应该从SecurityContext中获取
        return HttpResult.ok();
    }

    /**
     * 管理员获取用户列表
     * @return 用户列表
     */
    @GetMapping("/user/list")
    public HttpResult adminListUsers() {
        var users = userService.listAllUsers();
        log.debug("管理员查询用户列表，用户数量：{}", users.size());
        return HttpResult.ok(users);
    }

    /**
     * 管理员获取订单列表
     * @return 订单列表
     */
    @GetMapping("/order/list")
    public HttpResult adminListOrder() {
        var orders = orderService.listAllOrders();
        log.debug("管理员查询订单列表，订单数量：{}", orders.size());
        return HttpResult.ok(orders);
    }

}
