package com.example.demo.controller;

import com.example.demo.domain.Admin;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AdminService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;




    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public HttpResult login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return HttpResult.error("用户名和密码不能为空");
        }

        try {
            Admin admin = adminService.login(username, password);
            // 生成token（使用adminId作为subject）
            String token = jwtUtil.generateToken("admin_" + admin.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", admin);
            return HttpResult.ok(result);
        } catch (RuntimeException e) {
            return HttpResult.error(e.getMessage());
        }
    }

    @GetMapping("/current")
    public HttpResult getCurrent() {
        // 这里应该从token中获取adminId，暂时返回示例
        // 实际应该从SecurityContext中获取
        return HttpResult.ok();
    }


    // 管理后台接口
    @GetMapping("/user/list")
    public HttpResult adminListUsers() {
        return HttpResult.ok(userService.listAllUsers());
    }

    //订单后台接口
    @GetMapping("/order/list")
    public HttpResult adminListOrder() {
        return HttpResult.ok(orderService.listAllOrders());
    }

    //分类
//    @GetMapping("/category/list")
//    public HttpResult adminListCategory() {
//        return HttpResult.ok(categoryService.listAllCategory());
//    }

    //商品
    @GetMapping("/product/list")
    public HttpResult adminListProducts() {
        return HttpResult.ok(productService.listAllProducts());
    }
}
