package com.example.demo.controller;

import com.example.demo.domain.Admin;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AdminService;
import com.example.demo.service.LogService;
import com.example.demo.service.OrderService;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import com.example.demo.service.StatisticsService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.vo.PageRequestVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final StatisticsService statisticsService;
    private final LogService logService;

    /**
     * 管理员登录
     * @param request 登录请求（包含username和password）
     * @return 登录结果（token和admin信息）
     */
    @PostMapping("/login")
    public HttpResult login(@RequestBody Map<String, String> request, 
                           @RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor, 
                           @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            log.warn("管理员登录失败：用户名或密码为空");
            return HttpResult.error("用户名和密码不能为空");
        }

        // 获取客户端IP地址
        String ip = getClientIp(xForwardedFor);

        try {
            Admin admin = adminService.login(username, password);
            // 生成token（使用adminId作为subject）
            String token = jwtUtil.generateToken("admin_" + admin.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", admin);
            
            // 记录登录成功日志
            logService.recordLoginLog(admin.getId(), username, ip, userAgent, 1, null);
            log.info("管理员登录成功，adminId：{}，username：{}", admin.getId(), username);
            return HttpResult.ok(result);
        } catch (RuntimeException e) {
            // 记录登录失败日志
            logService.recordLoginLog(null, username, ip, userAgent, 0, e.getMessage());
            log.warn("管理员登录失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        }
    }

    // 辅助方法：获取客户端IP地址
    private String getClientIp(String xForwardedFor) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 处理X-Forwarded-For头，获取第一个IP
            return xForwardedFor.split(",")[0].trim();
        }
        // 如果没有X-Forwarded-For头，使用默认IP
        return "127.0.0.1";
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
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/user/detail/{id}")
    public HttpResult getUserDetail(@PathVariable Integer id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                return HttpResult.error("用户不存在");
            }
            log.debug("获取用户详情，userId：{}", id);
            return HttpResult.ok(user);
        } catch (Exception e) {
            log.error("获取用户详情失败：{}", e.getMessage());
            return HttpResult.error("获取用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 编辑用户信息
     * @param user 用户信息
     * @return 编辑结果
     */
    @PutMapping("/user/edit")
    public HttpResult editUser(@RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(user);
            log.info("编辑用户信息成功，userId：{}", user.getId());
            return HttpResult.ok(updatedUser);
        } catch (Exception e) {
            log.error("编辑用户信息失败：{}", e.getMessage());
            return HttpResult.error("编辑用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户状态
     * @param id 用户ID
     * @param status 状态（1-启用，0-禁用）
     * @return 更新结果
     */
    @PutMapping("/user/status/{id}")
    public HttpResult updateUserStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            userService.updateUserStatus(id, status);
            log.info("更新用户状态成功，userId：{}，status：{}", id, status);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage());
            return HttpResult.error("更新用户状态失败：" + e.getMessage());
        }
    }

    // ========== 角色管理 ==========

    /**
     * 获取角色列表
     * @return 角色列表
     */
    @GetMapping("/role/list")
    public HttpResult listRoles() {
        var roles = roleService.listAllRoles();
        log.debug("查询角色列表，数量：{}", roles.size());
        return HttpResult.ok(roles);
    }

    /**
     * 添加角色
     * @param role 角色信息
     * @return 添加结果
     */
    @PostMapping("/role/add")
    public HttpResult addRole(@RequestBody Role role) {
        try {
            Role savedRole = roleService.save(role);
            log.info("添加角色成功，roleId：{}，name：{}", savedRole.getId(), savedRole.getName());
            return HttpResult.ok(savedRole);
        } catch (Exception e) {
            log.error("添加角色失败：{}", e.getMessage());
            return HttpResult.error("添加角色失败：" + e.getMessage());
        }
    }

    /**
     * 编辑角色
     * @param role 角色信息
     * @return 编辑结果
     */
    @PutMapping("/role/edit")
    public HttpResult editRole(@RequestBody Role role) {
        try {
            Role updatedRole = roleService.save(role);
            log.info("编辑角色成功，roleId：{}，name：{}", updatedRole.getId(), updatedRole.getName());
            return HttpResult.ok(updatedRole);
        } catch (Exception e) {
            log.error("编辑角色失败：{}", e.getMessage());
            return HttpResult.error("编辑角色失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/role/delete/{id}")
    public HttpResult deleteRole(@PathVariable Integer id) {
        try {
            roleService.deleteById(id);
            log.info("删除角色成功，roleId：{}", id);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除角色失败：{}", e.getMessage());
            return HttpResult.error("删除角色失败：" + e.getMessage());
        }
    }

    /**
     * 更新角色状态
     * @param id 角色ID
     * @param status 状态（1-启用，0-禁用）
     * @return 更新结果
     */
    @PutMapping("/role/status/{id}")
    public HttpResult updateRoleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            roleService.updateStatus(id, status);
            log.info("更新角色状态成功，roleId：{}，status：{}", id, status);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新角色状态失败：{}", e.getMessage());
            return HttpResult.error("更新角色状态失败：" + e.getMessage());
        }
    }

    // ========== 权限管理 ==========

    /**
     * 获取权限列表
     * @return 权限列表
     */
    @GetMapping("/permission/list")
    public HttpResult listPermissions() {
        var permissions = permissionService.listAllPermissions();
        log.debug("查询权限列表，数量：{}", permissions.size());
        return HttpResult.ok(permissions);
    }

    /**
     * 添加权限
     * @param permission 权限信息
     * @return 添加结果
     */
    @PostMapping("/permission/add")
    public HttpResult addPermission(@RequestBody Permission permission) {
        try {
            Permission savedPermission = permissionService.save(permission);
            log.info("添加权限成功，permissionId：{}，name：{}", savedPermission.getId(), savedPermission.getName());
            return HttpResult.ok(savedPermission);
        } catch (Exception e) {
            log.error("添加权限失败：{}", e.getMessage());
            return HttpResult.error("添加权限失败：" + e.getMessage());
        }
    }

    /**
     * 编辑权限
     * @param permission 权限信息
     * @return 编辑结果
     */
    @PutMapping("/permission/edit")
    public HttpResult editPermission(@RequestBody Permission permission) {
        try {
            Permission updatedPermission = permissionService.save(permission);
            log.info("编辑权限成功，permissionId：{}，name：{}", updatedPermission.getId(), updatedPermission.getName());
            return HttpResult.ok(updatedPermission);
        } catch (Exception e) {
            log.error("编辑权限失败：{}", e.getMessage());
            return HttpResult.error("编辑权限失败：" + e.getMessage());
        }
    }

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/permission/delete/{id}")
    public HttpResult deletePermission(@PathVariable Integer id) {
        try {
            permissionService.deleteById(id);
            log.info("删除权限成功，permissionId：{}", id);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除权限失败：{}", e.getMessage());
            return HttpResult.error("删除权限失败：" + e.getMessage());
        }
    }

    // ========== 权限分配 ==========

    /**
     * 获取角色的权限
     * @param roleId 角色ID
     * @return 角色的权限列表
     */
    @GetMapping("/role/permissions/{roleId}")
    public HttpResult getRolePermissions(@PathVariable Integer roleId) {
        try {
            var permissions = permissionService.getByRoleId(roleId);
            log.debug("获取角色权限，roleId：{}，权限数量：{}", roleId, permissions.size());
            return HttpResult.ok(permissions);
        } catch (Exception e) {
            log.error("获取角色权限失败：{}", e.getMessage());
            return HttpResult.error("获取角色权限失败：" + e.getMessage());
        }
    }

    /**
     * 分配权限给角色
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/role/assign-permissions/{roleId}")
    public HttpResult assignPermissions(@PathVariable Integer roleId, @RequestBody Map<String, List<Integer>> request) {
        try {
            List<Integer> permissionIds = request.get("permissionIds");
            permissionService.assignPermissions(roleId, permissionIds);
            log.info("分配权限成功，roleId：{}，权限数量：{}", roleId, permissionIds != null ? permissionIds.size() : 0);
            return HttpResult.ok("权限分配成功");
        } catch (Exception e) {
            log.error("分配权限失败：{}", e.getMessage());
            return HttpResult.error("分配权限失败：" + e.getMessage());
        }
    }

    // ========== 系统统计 ==========

    /**
     * 获取系统概览统计
     * @return 系统概览数据
     */
    @GetMapping("/statistics/overview")
    public HttpResult getOverviewStatistics() {
        try {
            var statistics = statisticsService.getOverviewStatistics();
            log.debug("获取系统概览统计成功");
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取系统概览统计失败：{}", e.getMessage());
            return HttpResult.error("获取系统概览统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 订单统计数据
     */
    @GetMapping("/statistics/orders")
    public HttpResult getOrderStatistics(@RequestParam(required = false) String startTime, 
                                         @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getOrderStatistics(startTime, endTime);
            log.debug("获取订单统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取订单统计失败：{}", e.getMessage());
            return HttpResult.error("获取订单统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 销售统计数据
     */
    @GetMapping("/statistics/sales")
    public HttpResult getSalesStatistics(@RequestParam(required = false) String startTime, 
                                         @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getSalesStatistics(startTime, endTime);
            log.debug("获取销售统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取销售统计失败：{}", e.getMessage());
            return HttpResult.error("获取销售统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 用户统计数据
     */
    @GetMapping("/statistics/users")
    public HttpResult getUserStatistics(@RequestParam(required = false) String startTime, 
                                        @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getUserStatistics(startTime, endTime);
            log.debug("获取用户统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取用户统计失败：{}", e.getMessage());
            return HttpResult.error("获取用户统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取产品销售排名
     * @param limit 限制数量
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 产品销售排名数据
     */
    @GetMapping("/statistics/product-ranking")
    public HttpResult getProductSalesRanking(@RequestParam(defaultValue = "10") int limit, 
                                            @RequestParam(required = false) String startTime, 
                                            @RequestParam(required = false) String endTime) {
        try {
            var ranking = statisticsService.getProductSalesRanking(limit, startTime, endTime);
            log.debug("获取产品销售排名成功，限制数量：{}，时间范围：{} 至 {}", limit, startTime, endTime);
            return HttpResult.ok(ranking);
        } catch (Exception e) {
            log.error("获取产品销售排名失败：{}", e.getMessage());
            return HttpResult.error("获取产品销售排名失败：" + e.getMessage());
        }
    }

    // ========== 系统日志 ==========

    /**
     * 查询操作日志列表
     * @param pageRequest 分页请求
     * @param module 模块（可选）
     * @param adminId 管理员ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页的操作日志列表
     */
    @GetMapping("/logs/operation")
    public HttpResult listOperationLogs(PageRequestVO pageRequest, 
                                       @RequestParam(required = false) String module, 
                                       @RequestParam(required = false) Integer adminId, 
                                       @RequestParam(required = false) String startTime, 
                                       @RequestParam(required = false) String endTime) {
        try {
            var logs = logService.listOperationLogs(pageRequest, module, adminId, startTime, endTime);
            log.debug("查询操作日志列表成功，数量：{}", logs.getTotal());
            return HttpResult.ok(logs);
        } catch (Exception e) {
            log.error("查询操作日志列表失败：{}", e.getMessage());
            return HttpResult.error("查询操作日志列表失败：" + e.getMessage());
        }
    }

    /**
     * 查询登录日志列表
     * @param pageRequest 分页请求
     * @param username 用户名（可选）
     * @param ip IP地址（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页的登录日志列表
     */
    @GetMapping("/logs/login")
    public HttpResult listLoginLogs(PageRequestVO pageRequest, 
                                   @RequestParam(required = false) String username, 
                                   @RequestParam(required = false) String ip, 
                                   @RequestParam(required = false) String startTime, 
                                   @RequestParam(required = false) String endTime) {
        try {
            var logs = logService.listLoginLogs(pageRequest, username, ip, startTime, endTime);
            log.debug("查询登录日志列表成功，数量：{}", logs.getTotal());
            return HttpResult.ok(logs);
        } catch (Exception e) {
            log.error("查询登录日志列表失败：{}", e.getMessage());
            return HttpResult.error("查询登录日志列表失败：" + e.getMessage());
        }
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

    /**
     * 获取管理员列表
     * @return 管理员列表
     */
    @GetMapping("/list")
    public HttpResult listAdmins() {
        var admins = adminService.listAllAdmins();
        log.debug("查询管理员列表，数量：{}", admins.size());
        return HttpResult.ok(admins);
    }

    /**
     * 添加管理员
     * @param admin 管理员信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public HttpResult addAdmin(@RequestBody Admin admin) {
        try {
            Admin savedAdmin = adminService.save(admin);
            log.info("添加管理员成功，adminId：{}，username：{}", savedAdmin.getId(), savedAdmin.getUsername());
            return HttpResult.ok(savedAdmin);
        } catch (Exception e) {
            log.error("添加管理员失败：{}", e.getMessage());
            return HttpResult.error("添加管理员失败：" + e.getMessage());
        }
    }

    /**
     * 编辑管理员
     * @param admin 管理员信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    public HttpResult editAdmin(@RequestBody Admin admin) {
        try {
            Admin updatedAdmin = adminService.save(admin);
            log.info("编辑管理员成功，adminId：{}，username：{}", updatedAdmin.getId(), updatedAdmin.getUsername());
            return HttpResult.ok(updatedAdmin);
        } catch (Exception e) {
            log.error("编辑管理员失败：{}", e.getMessage());
            return HttpResult.error("编辑管理员失败：" + e.getMessage());
        }
    }

    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public HttpResult deleteAdmin(@PathVariable Integer id) {
        try {
            adminService.deleteAdmin(id);
            log.info("删除管理员成功，adminId：{}", id);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除管理员失败：{}", e.getMessage());
            return HttpResult.error("删除管理员失败：" + e.getMessage());
        }
    }

    /**
     * 更新管理员状态
     * @param id 管理员ID
     * @param status 状态（1-启用，0-禁用）
     * @return 更新结果
     */
    @PutMapping("/status/{id}")
    public HttpResult updateAdminStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            adminService.updateAdminStatus(id, status);
            log.info("更新管理员状态成功，adminId：{}，status：{}", id, status);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新管理员状态失败：{}", e.getMessage());
            return HttpResult.error("更新管理员状态失败：" + e.getMessage());
        }
    }

}
