package com.example.demo.controller;

import com.example.demo.domain.Admin;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.Permission;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AdminRoleService;
import com.example.demo.service.AdminService;
import com.example.demo.service.LogService;
import com.example.demo.service.OrderService;
import com.example.demo.service.PermissionService;
import com.example.demo.service.RoleService;
import com.example.demo.service.StatisticsService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.annotation.RequiresPermission;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminRoleService adminRoleService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final OrderService orderService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final StatisticsService statisticsService;
    private final LogService logService;

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);

            javax.servlet.http.HttpServletRequest request = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null
                ? ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest()
                : null;

            if (request != null) {
                operationLog.setMethod(request.getMethod() + " " + request.getRequestURI());
                operationLog.setIp(getClientIp(request));
                operationLog.setUserAgent(request.getHeader("User-Agent"));

                // 获取当前登录管理员ID
                String token = request.getHeader("Authorization");
                if (token != null && token.startsWith("Bearer ")) {
                    String tokenValue = token.substring(7);
                    Integer adminId = jwtUtil.getAdminIdFromToken(tokenValue);
                    operationLog.setAdminId(adminId);
                }
            }

            logService.recordOperationLog(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    private String getClientIp(javax.servlet.http.HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

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
            String token = jwtUtil.generateAdminToken(admin.getId());

            // 获取管理员角色
            List<Integer> roleIds = adminRoleService.getRoleIdsByAdminId(admin.getId());
            List<Map<String, Object>> roles = new ArrayList<>();
            for (Integer roleId : roleIds) {
                Role role = roleService.getById(roleId);
                if (role != null) {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    roles.add(roleMap);
                }
            }

            // 构建返回的admin对象
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getUsername());
            adminInfo.put("realName", admin.getRealName());
            adminInfo.put("roles", roles);

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("admin", adminInfo);

            // 记录登录成功日志
            logService.recordLoginLog(admin.getId(), username, ip, userAgent, 1, null);
            log.info("管理员登录成功，adminId：{}，username：{}", admin.getId(), username);
            return HttpResult.ok(result);
        } catch (RuntimeException e) {
            log.warn("管理员登录失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        }
    }

    @PostMapping("/register")
    public HttpResult register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String realName = request.get("realName");

            if (username == null || username.trim().isEmpty()) {
                return HttpResult.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return HttpResult.error("密码不能为空");
            }
            if (realName == null || realName.trim().isEmpty()) {
                return HttpResult.error("真实姓名不能为空");
            }

            Admin admin = adminService.register(username, password, realName);
            log.info("管理员注册成功，adminId：{}，username：{}", admin.getId(), username);
            return HttpResult.ok("注册成功", admin);
        } catch (RuntimeException e) {
            log.warn("管理员注册失败：{}", e.getMessage());
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
    public HttpResult getCurrent(HttpServletRequest request) {
        try {
            // 从请求头获取token
            String token = request.getHeader("Authorization");
            if (!StringUtils.hasText(token)) {
                return HttpResult.error("未登录或登录已过期");
            }

            // 从token中获取管理员ID
            Integer adminId = jwtUtil.getAdminIdFromToken(token.replace("Bearer ", ""));
            if (adminId == null) {
                return HttpResult.error("无效的token");
            }

            // 获取管理员信息
            Admin admin = adminService.getById(adminId);
            if (admin == null) {
                return HttpResult.error("管理员不存在");
            }

            // 获取管理员角色
            List<Integer> roleIds = adminRoleService.getRoleIdsByAdminId(admin.getId());
            List<Map<String, Object>> roles = new ArrayList<>();
            for (Integer roleId : roleIds) {
                Role role = roleService.getById(roleId);
                if (role != null) {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    roles.add(roleMap);
                }
            }

            // 获取管理员权限
            List<Permission> permissions = permissionService.getPermissionsByAdminId(admin.getId());
            List<Map<String, Object>> permissionList = new ArrayList<>();
            for (Permission permission : permissions) {
                Map<String, Object> permMap = new HashMap<>();
                permMap.put("id", permission.getId());
                permMap.put("code", permission.getCode());
                permMap.put("name", permission.getName());
                permMap.put("resource", permission.getResource());
                permMap.put("action", permission.getAction());
                permissionList.add(permMap);
            }

            // 构建返回的管理员信息
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getUsername());
            adminInfo.put("realName", admin.getRealName());
            adminInfo.put("roles", roles);
            adminInfo.put("permissions", permissionList);

            log.info("获取当前管理员信息成功，adminId：{}", admin.getId());
            return HttpResult.ok(adminInfo);
        } catch (Exception e) {
            log.error("获取当前管理员信息失败：{}", e.getMessage());
            return HttpResult.error("获取当前管理员信息失败：" + e.getMessage());
        }
    }

    /**
     * 管理员获取用户列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @param status 用户状态
     * @return 用户列表
     */
    @GetMapping("/user/list")
    @RequiresPermission(code = "user:read")
    public HttpResult adminListUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            var users = userService.getUserListByPage(pageRequest, keyword, status);
            log.debug("管理员查询用户列表，页码：{}，每页大小：{}，总记录数：{}", page, pageSize, users.getTotal());
            recordOperationLog("用户管理", "查询用户列表", 1, null);
            return HttpResult.ok(users);
        } catch (Exception e) {
            log.error("查询用户列表失败：{}", e.getMessage());
            recordOperationLog("用户管理", "查询用户列表", 0, e.getMessage());
            return HttpResult.error("查询用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/user/detail/{id}")
    @RequiresPermission(code = "user:read")
    public HttpResult getUserDetail(@PathVariable Integer id) {
        try {
            User user = userService.getById(id);
            if (user == null) {
                recordOperationLog("用户管理", "查看用户详情", 0, "用户不存在");
                return HttpResult.error("用户不存在");
            }
            log.debug("获取用户详情，userId：{}", id);
            recordOperationLog("用户管理", "查看用户详情", 1, null);
            return HttpResult.ok(user);
        } catch (Exception e) {
            log.error("获取用户详情失败：{}", e.getMessage());
            recordOperationLog("用户管理", "查看用户详情", 0, e.getMessage());
            return HttpResult.error("获取用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 编辑用户信息
     * @param user 用户信息
     * @return 编辑结果
     */
    @PutMapping("/user/edit")
    @RequiresPermission(code = "user:manage")
    public HttpResult editUser(@RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(user);
            log.info("编辑用户信息成功，userId：{}", user.getId());
            recordOperationLog("用户管理", "编辑用户信息", 1, null);
            return HttpResult.ok(updatedUser);
        } catch (Exception e) {
            log.error("编辑用户信息失败：{}", e.getMessage());
            recordOperationLog("用户管理", "编辑用户信息", 0, e.getMessage());
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
    @RequiresPermission(code = "user:manage")
    public HttpResult updateUserStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            userService.updateUserStatus(id, status);
            log.info("更新用户状态成功，userId：{}，status：{}", id, status);
            recordOperationLog("用户管理", "更新用户状态", 1, null);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage());
            recordOperationLog("用户管理", "更新用户状态", 0, e.getMessage());
            return HttpResult.error("更新用户状态失败：" + e.getMessage());
        }
    }

    // ========== 角色管理 ==========

    /**
     * 获取角色列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词
     * @return 分页角色列表
     */
    @GetMapping("/role/list")
    @RequiresPermission( code = "role:read")
    public HttpResult listRoles(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            var roles = roleService.getRoleListByPage(pageRequest, keyword);
            log.debug("查询角色列表，页码：{}，每页大小：{}，总记录数：{}",
                    pageRequest.getPageNum(), pageRequest.getPageSize(), roles.getTotal());
            recordOperationLog("角色管理", "查询角色列表", 1, null);
            return HttpResult.ok(roles);
        } catch (Exception e) {
            log.error("查询角色列表失败：{}", e.getMessage());
            recordOperationLog("角色管理", "查询角色列表", 0, e.getMessage());
            return HttpResult.error("查询角色列表失败：" + e.getMessage());
        }
    }

    /**
     * 添加角色
     * @param role 角色信息
     * @return 添加结果
     */
    @PostMapping("/role/add")
    @RequiresPermission(code = "role:manage")
    public HttpResult addRole(@RequestBody Role role) {
        try {
            Role savedRole = roleService.save(role);
            log.info("添加角色成功，roleId：{}，name：{}", savedRole.getId(), savedRole.getName());
            recordOperationLog("角色管理", "添加角色", 1, null);
            return HttpResult.ok(savedRole);
        } catch (Exception e) {
            log.error("添加角色失败：{}", e.getMessage());
            recordOperationLog("角色管理", "添加角色", 0, e.getMessage());
            return HttpResult.error("添加角色失败：" + e.getMessage());
        }
    }

    /**
     * 编辑角色
     * @param role 角色信息
     * @return 编辑结果
     */
    @PutMapping("/role/edit")
    @RequiresPermission(code = "role:manage")
    public HttpResult editRole(@RequestBody Role role) {
        try {
            Role updatedRole = roleService.save(role);
            log.info("编辑角色成功，roleId：{}，name：{}", updatedRole.getId(), updatedRole.getName());
            recordOperationLog("角色管理", "编辑角色", 1, null);
            return HttpResult.ok(updatedRole);
        } catch (Exception e) {
            log.error("编辑角色失败：{}", e.getMessage());
            recordOperationLog("角色管理", "编辑角色", 0, e.getMessage());
            return HttpResult.error("编辑角色失败：" + e.getMessage());
        }
    }

    /**
     * 删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    @DeleteMapping("/role/delete/{id}")
    @RequiresPermission(code = "role:manage")
    public HttpResult deleteRole(@PathVariable Integer id) {
        try {
            roleService.deleteById(id);
            log.info("删除角色成功，roleId：{}", id);
            recordOperationLog("角色管理", "删除角色", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除角色失败：{}", e.getMessage());
            recordOperationLog("角色管理", "删除角色", 0, e.getMessage());
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
    @RequiresPermission(code = "role:manage")
    public HttpResult updateRoleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            roleService.updateStatus(id, status);
            log.info("更新角色状态成功，roleId：{}，status：{}", id, status);
            recordOperationLog("角色管理", "更新角色状态", 1, null);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新角色状态失败：{}", e.getMessage());
            recordOperationLog("角色管理", "更新角色状态", 0, e.getMessage());
            return HttpResult.error("更新角色状态失败：" + e.getMessage());
        }
    }

    // ========== 权限管理 ==========

    /**
     * 获取权限列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return 分页权限列表
     */
    @GetMapping("/permission/list")
    @RequiresPermission(code = "permission:read")
    public HttpResult listPermissions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            pageRequest.setOrderBy(orderBy);
            pageRequest.setOrderDirection(orderDirection);

            var permissions = permissionService.getPermissionListByPage(pageRequest);
            log.info("查询权限列表，页码：{}，每页大小：{}，总记录数：{}",
                    pageRequest.getPageNum(), pageRequest.getPageSize(), permissions.getTotal());
            recordOperationLog("权限管理", "查询权限列表", 1, null);
            return HttpResult.ok(permissions);
        } catch (Exception e) {
            log.error("查询权限列表失败：{}", e.getMessage());
            recordOperationLog("权限管理", "查询权限列表", 0, e.getMessage());
            return HttpResult.error("查询权限列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据名称搜索权限（分页）
     * @param name 权限名称
     * @param page 页码
     * @param pageSize 每页大小
     * @param orderBy 排序字段
     * @param orderDirection 排序方向
     * @return 分页权限列表
     */
    @GetMapping("/permission/search")
    @RequiresPermission(code = "permission:read")
    public HttpResult searchPermissions(
            @RequestParam(required = true) String name,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDirection) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            pageRequest.setOrderBy(orderBy);
            pageRequest.setOrderDirection(orderDirection);

            var permissions = permissionService.searchPermissionsByName(pageRequest, name);
            log.info("搜索权限，名称：{}，页码：{}，每页大小：{}，总记录数：{}",
                    name, pageRequest.getPageNum(), pageRequest.getPageSize(), permissions.getTotal());
            recordOperationLog("权限管理", "搜索权限", 1, null);
            return HttpResult.ok(permissions);
        } catch (Exception e) {
            log.error("搜索权限失败：{}", e.getMessage());
            recordOperationLog("权限管理", "搜索权限", 0, e.getMessage());
            return HttpResult.error("搜索权限失败：" + e.getMessage());
        }
    }

    /**
     * 添加权限
     * @param permission 权限信息
     * @return 添加结果
     */
    @PostMapping("/permission/add")
    @RequiresPermission(code = "permission:manage")
    public HttpResult addPermission(@RequestBody Permission permission) {
        try {
            Permission savedPermission = permissionService.save(permission);
            log.info("添加权限成功，permissionId：{}，name：{}", savedPermission.getId(), savedPermission.getName());
            recordOperationLog("权限管理", "添加权限", 1, null);
            return HttpResult.ok(savedPermission);
        } catch (Exception e) {
            log.error("添加权限失败：{}", e.getMessage());
            recordOperationLog("权限管理", "添加权限", 0, e.getMessage());
            return HttpResult.error("添加权限失败：" + e.getMessage());
        }
    }

    /**
     * 编辑权限
     * @param permission 权限信息
     * @return 编辑结果
     */
    @PutMapping("/permission/edit")
    @RequiresPermission(code = "permission:manage")
    public HttpResult editPermission(@RequestBody Permission permission) {
        try {
            Permission updatedPermission = permissionService.save(permission);
            log.info("编辑权限成功，permissionId：{}，name：{}", updatedPermission.getId(), updatedPermission.getName());
            recordOperationLog("权限管理", "编辑权限", 1, null);
            return HttpResult.ok(updatedPermission);
        } catch (Exception e) {
            log.error("编辑权限失败：{}", e.getMessage());
            recordOperationLog("权限管理", "编辑权限", 0, e.getMessage());
            return HttpResult.error("编辑权限失败：" + e.getMessage());
        }
    }

    /**
     * 删除权限
     * @param id 权限ID
     * @return 删除结果
     */
    @DeleteMapping("/permission/delete/{id}")
    @RequiresPermission(code = "permission:manage")
    public HttpResult deletePermission(@PathVariable Integer id) {
        try {
            permissionService.deleteById(id);
            log.info("删除权限成功，permissionId：{}", id);
            recordOperationLog("权限管理", "删除权限", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除权限失败：{}", e.getMessage());
            recordOperationLog("权限管理", "删除权限", 0, e.getMessage());
            return HttpResult.error("删除权限失败：" + e.getMessage());
        }
    }



    /**
     * 更新权限状态
     * @param id 权限ID
     * @param status 状态（1=启用，0=禁用）
     * @return 更新结果
     */
    @PutMapping("/permission/status/{id}")
    @RequiresPermission(code = "permission:manage")
    public HttpResult updatePermissionStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            permissionService.updateStatus(id, status);
            log.info("更新权限状态成功，permissionId：{}，status：{}", id, status);
            recordOperationLog("权限管理", "更新权限状态", 1, null);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新权限状态失败：{}", e.getMessage());
            recordOperationLog("权限管理", "更新权限状态", 0, e.getMessage());
            return HttpResult.error("更新权限状态失败：" + e.getMessage());
        }
    }

    // ========== 权限分配 ==========

    /**
     * 获取角色的权限
     * @param roleId 角色ID
     * @return 角色的权限列表
     */
    @GetMapping("/role/permissions/{roleId}")
    @RequiresPermission(code = "role:manage")
    public HttpResult getRolePermissions(@PathVariable Integer roleId) {
        try {
            var permissions = permissionService.getByRoleId(roleId);
            log.info("获取角色权限，roleId：{}，权限数量：{}", roleId, permissions.size());
            recordOperationLog("角色权限", "获取角色权限", 1, null);
            return HttpResult.ok(permissions);
        } catch (Exception e) {
            log.error("获取角色权限失败：{}", e.getMessage());
            return HttpResult.error("获取角色权限失败：" + e.getMessage());
        }
    }

    /**
     * 分配权限给角色
     * @param roleId 角色ID
     * @param request 权限ID列表
     * @return 分配结果
     */
    @PostMapping("/role/assign-permissions/{roleId}")
    @RequiresPermission(code = "role:manage")
    public HttpResult assignPermissions(@PathVariable Integer roleId, @RequestBody Map<String, List<Integer>> request) {
        try {
            List<Integer> permissionIds = request.get("permissionIds");
            permissionService.assignPermissions(roleId, permissionIds);
            log.info("分配权限成功，roleId：{}，权限数量：{}", roleId, permissionIds != null ? permissionIds.size() : 0);
            recordOperationLog("角色权限", "分配权限", 1, null);
            return HttpResult.ok("权限分配成功");
        } catch (Exception e) {
            log.error("分配权限失败：{}", e.getMessage());
            recordOperationLog("角色权限", "分配权限", 0, e.getMessage());
            return HttpResult.error("分配权限失败：" + e.getMessage());
        }
    }

    // ========== 系统统计 ==========

    /**
     * 获取系统概览统计
     * @return 系统概览数据
     */
    @GetMapping("/statistics/overview")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getOverviewStatistics() {
        try {
            var statistics = statisticsService.getOverviewStatistics();
            log.info("获取系统概览统计成功");
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
    @RequiresPermission(code = "statistics:read")
    public HttpResult getOrderStatistics(@RequestParam(required = false) String startTime,
                                         @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getOrderStatistics(startTime, endTime);
            log.info("获取订单统计成功，时间范围：{} 至 {}", startTime, endTime);
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
    @RequiresPermission(code = "statistics:read")
    public HttpResult getSalesStatistics(@RequestParam(required = false) String startTime,
                                         @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getSalesStatistics(startTime, endTime);
            log.info("获取销售统计成功，时间范围：{} 至 {}", startTime, endTime);
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
    @RequiresPermission(code = "statistics:read")
    public HttpResult getUserStatistics(@RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getUserStatistics(startTime, endTime);
            log.info("获取用户统计成功，时间范围：{} 至 {}", startTime, endTime);
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
    @RequiresPermission(code = "statistics:read")
    public HttpResult getProductSalesRanking(@RequestParam(defaultValue = "10") int limit,
                                            @RequestParam(required = false) String startTime,
                                            @RequestParam(required = false) String endTime) {
        try {
            var ranking = statisticsService.getProductSalesRanking(limit, startTime, endTime);
            log.info("获取产品销售排名成功，限制数量：{}，时间范围：{} 至 {}", limit, startTime, endTime);
            return HttpResult.ok(ranking);
        } catch (Exception e) {
            log.error("获取产品销售排名失败：{}", e.getMessage());
            return HttpResult.error("获取产品销售排名失败：" + e.getMessage());
        }
    }

    /**
     * 获取分类销售统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分类销售统计数据
     */
    @GetMapping("/statistics/category-sales")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getCategorySalesStatistics(@RequestParam(required = false) String startTime,
                                               @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getCategorySalesStatistics(startTime, endTime);
            log.info("获取分类销售统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取分类销售统计失败：{}", e.getMessage());
            return HttpResult.error("获取分类销售统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取支付方式统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 支付方式统计数据
     */
    @GetMapping("/statistics/payment-methods")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getPaymentMethodStatistics(@RequestParam(required = false) String startTime,
                                               @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getPaymentMethodStatistics(startTime, endTime);
            log.info("获取支付方式统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取支付方式统计失败：{}", e.getMessage());
            return HttpResult.error("获取支付方式统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售趋势统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 销售趋势统计数据
     */
    @GetMapping("/statistics/sales-trend")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getSalesTrendStatistics(@RequestParam(required = false) String startTime,
                                             @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getSalesTrendStatistics(startTime, endTime);
            log.info("获取销售趋势统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取销售趋势统计失败：{}", e.getMessage());
            return HttpResult.error("获取销售趋势统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取订单状态统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 订单状态统计数据
     */
    @GetMapping("/statistics/order-status")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getOrderStatusStatistics(@RequestParam(required = false) String startTime,
                                              @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getOrderStatusStatistics(startTime, endTime);
            log.info("获取订单状态统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取订单状态统计失败：{}", e.getMessage());
            return HttpResult.error("获取订单状态统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户活动统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 用户活动统计数据
     */
    @GetMapping("/statistics/user-activity")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getUserActivityStatistics(@RequestParam(required = false) String startTime,
                                               @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getUserActivityStatistics(startTime, endTime);
            log.info("获取用户活动统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取用户活动统计失败：{}", e.getMessage());
            return HttpResult.error("获取用户活动统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取销售时段统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 销售时段统计数据
     */
    @GetMapping("/statistics/sales-by-hour")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getSalesByHourStatistics(@RequestParam(required = false) String startTime,
                                              @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getSalesByHourStatistics(startTime, endTime);
            log.info("获取销售时段统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取销售时段统计失败：{}", e.getMessage());
            return HttpResult.error("获取销售时段统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取平均订单价值统计
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 平均订单价值统计数据
     */
    @GetMapping("/statistics/average-order-value")
    @RequiresPermission(code = "statistics:read")
    public HttpResult getAverageOrderValueStatistics(@RequestParam(required = false) String startTime,
                                                    @RequestParam(required = false) String endTime) {
        try {
            var statistics = statisticsService.getAverageOrderValueStatistics(startTime, endTime);
            log.info("获取平均订单价值统计成功，时间范围：{} 至 {}", startTime, endTime);
            return HttpResult.ok(statistics);
        } catch (Exception e) {
            log.error("获取平均订单价值统计失败：{}", e.getMessage());
            return HttpResult.error("获取平均订单价值统计失败：" + e.getMessage());
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
    @RequiresPermission(code = "log:read")
    public HttpResult listOperationLogs(PageRequestVO pageRequest,
                                       @RequestParam(required = false) String module,
                                       @RequestParam(required = false) String action,
                                       @RequestParam(required = false) String content,
                                       @RequestParam(required = false) Integer adminId,
                                       @RequestParam(required = false) String startTime,
                                       @RequestParam(required = false) String endTime) {
        try {
            var logs = logService.listOperationLogs(pageRequest, module, action, content, adminId, startTime, endTime);
            log.info("查询操作日志列表成功，数量：{}", logs.getTotal());
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
    @RequiresPermission(code = "log:read")
    public HttpResult listLoginLogs(PageRequestVO pageRequest,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String ip,
                                   @RequestParam(required = false) Integer status,
                                   @RequestParam(required = false) String startTime,
                                   @RequestParam(required = false) String endTime) {
        try {
            var logs = logService.listLoginLogs(pageRequest, username, ip, status, startTime, endTime);
            log.info("查询登录日志列表成功，数量：{}", logs.getTotal());
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
    @RequiresPermission(code = "order:read")
    public HttpResult adminListOrder() {
        var orders = orderService.listAllOrders();
        log.info("管理员查询订单列表，订单数量：{}", orders.size());
        recordOperationLog("订单管理", "查询订单列表", 1, null);
        return HttpResult.ok(orders);
    }

    /**
     * 管理员获取订单详情
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/order/detail/{orderId}")
    @RequiresPermission(code = "order:read")
    public HttpResult adminGetOrderDetail(@PathVariable Integer orderId) {
        try {
            OrderDetailVO orderDetail = orderService.getOrderDetailVO(orderId);
            if (orderDetail == null) {
                recordOperationLog("订单管理", "查看订单详情", 0, "订单不存在");
                return HttpResult.error("订单不存在");
            }
            log.info("管理员查询订单详情，orderId：{}", orderId);
            recordOperationLog("订单管理", "查看订单详情", 1, null);
            return HttpResult.ok(orderDetail);
        } catch (Exception e) {
            log.error("获取订单详情失败：{}", e.getMessage());
            recordOperationLog("订单管理", "查看订单详情", 0, e.getMessage());
            return HttpResult.error("获取订单详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取管理员列表（分页，支持搜索）
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词（用户名）
     * @return 分页管理员列表
     */
    @GetMapping("/list")
    @RequiresPermission(code = "admin:read")
    public HttpResult listAdmins(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            var admins = keyword != null && !keyword.trim().isEmpty()
                    ? adminService.searchAdminListByPage(pageRequest, keyword)
                    : adminService.getAdminListByPage(pageRequest);

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Admin admin : admins.getRecords()) {
                List<Integer> roleIds = adminRoleService.getRoleIdsByAdminId(admin.getId());
                List<Role> roles = new ArrayList<>();
                for (Integer roleId : roleIds) {
                    Role role = roleService.getById(roleId);
                    if (role != null) {
                        roles.add(role);
                    }
                }
                admin.setRoleId(roleIds.isEmpty() ? null : roleIds.get(0));

                Map<String, Object> adminMap = new HashMap<>();
                adminMap.put("id", admin.getId());
                adminMap.put("username", admin.getUsername());
                adminMap.put("realName", admin.getRealName());
                adminMap.put("phone", admin.getPhone());
                adminMap.put("roleId", admin.getRoleId());
                adminMap.put("status", admin.getStatus());
                adminMap.put("createTime", admin.getCreateTime());
                adminMap.put("updateTime", admin.getUpdateTime());
                adminMap.put("roles", roles);
                resultList.add(adminMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", resultList);
            result.put("total", admins.getTotal());
            result.put("pageNum", admins.getPageNum());
            result.put("pageSize", admins.getPageSize());
            result.put("pages", admins.getPages());
            result.put("hasPrevious", admins.getHasPrevious());
            result.put("hasNext", admins.getHasNext());

            log.info("查询管理员列表，关键词：{}，页码：{}，每页大小：{}，总记录数：{}",
                    keyword, pageRequest.getPageNum(), pageRequest.getPageSize(), admins.getTotal());
            recordOperationLog("管理员管理", "查询管理员列表", 1, null);
            return HttpResult.ok(result);
        } catch (Exception e) {
            log.error("查询管理员列表失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "查询管理员列表", 0, e.getMessage());
            return HttpResult.error("查询管理员列表失败：" + e.getMessage());
        }
    }

    /**
     * 搜索管理员列表（分页，根据用户名搜索）
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键词（用户名）
     * @return 分页管理员列表
     */
    @GetMapping("/search")
    @RequiresPermission(code = "admin:read")
    public HttpResult searchAdmins(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            var admins = adminService.searchAdminListByPage(pageRequest, keyword);

            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Admin admin : admins.getRecords()) {
                List<Integer> roleIds = adminRoleService.getRoleIdsByAdminId(admin.getId());
                List<Role> roles = new ArrayList<>();
                for (Integer roleId : roleIds) {
                    Role role = roleService.getById(roleId);
                    if (role != null) {
                        roles.add(role);
                    }
                }
                admin.setRoleId(roleIds.isEmpty() ? null : roleIds.get(0));

                Map<String, Object> adminMap = new HashMap<>();
                adminMap.put("id", admin.getId());
                adminMap.put("username", admin.getUsername());
                adminMap.put("realName", admin.getRealName());
                adminMap.put("phone", admin.getPhone());
                adminMap.put("roleId", admin.getRoleId());
                adminMap.put("status", admin.getStatus());
                adminMap.put("createTime", admin.getCreateTime());
                adminMap.put("updateTime", admin.getUpdateTime());
                adminMap.put("roles", roles);
                resultList.add(adminMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("records", resultList);
            result.put("total", admins.getTotal());
            result.put("pageNum", admins.getPageNum());
            result.put("pageSize", admins.getPageSize());
            result.put("pages", admins.getPages());
            result.put("hasPrevious", admins.getHasPrevious());
            result.put("hasNext", admins.getHasNext());

            log.info("搜索管理员列表，关键词：{}，页码：{}，每页大小：{}，总记录数：{}",
                    keyword, pageRequest.getPageNum(), pageRequest.getPageSize(), admins.getTotal());
            recordOperationLog("管理员管理", "搜索管理员列表", 1, null);
            return HttpResult.ok(result);
        } catch (Exception e) {
            log.error("搜索管理员列表失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "搜索管理员列表", 0, e.getMessage());
            return HttpResult.error("搜索管理员列表失败：" + e.getMessage());
        }
    }

    /**
     * 添加管理员
     * @param admin 管理员信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequiresPermission(code = "admin:manage")
    public HttpResult addAdmin(@RequestBody Admin admin) {
        try {
            Admin savedAdmin = adminService.save(admin);
            log.info("添加管理员成功，adminId：{}，username：{}", savedAdmin.getId(), savedAdmin.getUsername());
            recordOperationLog("管理员管理", "添加管理员", 1, null);
            return HttpResult.ok(savedAdmin);
        } catch (Exception e) {
            log.error("添加管理员失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "添加管理员", 0, e.getMessage());
            return HttpResult.error("添加管理员失败：" + e.getMessage());
        }
    }

    /**
     * 编辑管理员
     * @param admin 管理员信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @RequiresPermission(code = "admin:manage")
    public HttpResult editAdmin(@RequestBody Admin admin) {
        try {
            Admin updatedAdmin = adminService.save(admin);
            log.info("编辑管理员成功，adminId：{}，username：{}", updatedAdmin.getId(), updatedAdmin.getUsername());
            recordOperationLog("管理员管理", "编辑管理员", 1, null);
            return HttpResult.ok(updatedAdmin);
        } catch (Exception e) {
            log.error("编辑管理员失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "编辑管理员", 0, e.getMessage());
            return HttpResult.error("编辑管理员失败：" + e.getMessage());
        }
    }

    /**
     * 删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    @RequiresPermission(code = "admin:manage")
    public HttpResult deleteAdmin(@PathVariable Integer id) {
        try {
            adminService.deleteAdmin(id);
            log.info("删除管理员成功，adminId：{}", id);
            recordOperationLog("管理员管理", "删除管理员", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除管理员失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "删除管理员", 0, e.getMessage());
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
    @RequiresPermission(code = "admin:manage")
    public HttpResult updateAdminStatus(@PathVariable Integer id, @RequestParam Integer status) {
        try {
            adminService.updateAdminStatus(id, status);
            log.info("更新管理员状态成功，adminId：{}，status：{}", id, status);
            recordOperationLog("管理员管理", "更新管理员状态", 1, null);
            return HttpResult.ok("状态更新成功");
        } catch (Exception e) {
            log.error("更新管理员状态失败：{}", e.getMessage());
            recordOperationLog("管理员管理", "更新管理员状态", 0, e.getMessage());
            return HttpResult.error("更新管理员状态失败：" + e.getMessage());
        }
    }

    // ========== RBAC：管理员角色分配 ==========

    /**
     * 获取管理员的角色ID列表
     * @param adminId 管理员ID
     * @return 角色ID列表
     */
    @GetMapping("/roles/{adminId}")
    @RequiresPermission(code = "admin:manage")
    public HttpResult getAdminRoles(@PathVariable Integer adminId) {
        try {
            var roleIds = adminRoleService.getRoleIdsByAdminId(adminId);
            recordOperationLog("管理员角色", "获取管理员角色", 1, null);
            return HttpResult.ok(roleIds);
        } catch (Exception e) {
            log.error("获取管理员角色失败：{}", e.getMessage());
            recordOperationLog("管理员角色", "获取管理员角色", 0, e.getMessage());
            return HttpResult.error("获取管理员角色失败：" + e.getMessage());
        }
    }

    /**
     * 为管理员分配角色（一个账号只能分配一个角色）
     * @param adminId 管理员ID
     * @param request 请求体，包含 roleId
     * @return 分配结果
     */
    @PostMapping("/assign-roles/{adminId}")
    @RequiresPermission(code = "admin:manage")
    public HttpResult assignRoles(@PathVariable Integer adminId, @RequestBody Map<String, Integer> request) {
        try {
            Integer roleId = request.get("roleId");
            adminRoleService.assignRole(adminId, roleId);
            log.info("分配管理员角色成功，adminId：{}，roleId：{}", adminId, roleId);
            recordOperationLog("管理员角色", "分配管理员角色", 1, null);
            return HttpResult.ok("角色分配成功");
        } catch (Exception e) {
            log.error("分配管理员角色失败：{}", e.getMessage());
            recordOperationLog("管理员角色", "分配管理员角色", 0, e.getMessage());
            return HttpResult.error("分配管理员角色失败：" + e.getMessage());
        }
    }

    /**
     * 测试权限验证
     * @return 测试结果
     */
    @GetMapping("/test/permission")
    @RequiresPermission(code = "test:read")
    public HttpResult testPermission() {
        try {
            log.info("权限验证测试成功");
            return HttpResult.ok("权限验证测试成功");
        } catch (Exception e) {
            log.error("权限验证测试失败：{}", e.getMessage());
            return HttpResult.error("权限验证测试失败：" + e.getMessage());
        }
    }

}
