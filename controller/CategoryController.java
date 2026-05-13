package com.example.demo.controller;

import com.example.demo.domain.Category;
import com.example.demo.domain.OperationLog;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.service.LogService;
import com.example.demo.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import com.example.demo.annotation.RequiresPermission;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryMapper categoryMapper;
    private final LogService logService;
    private final JwtUtil jwtUtil;

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);
            
            HttpServletRequest request = org.springframework.web.context.request.RequestContextHolder.getRequestAttributes() != null 
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

    private String getClientIp(HttpServletRequest request) {
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

    @GetMapping("/admin/category/list")
    @RequiresPermission(code = "category:read")
    public HttpResult getAll() {
        try {
            List<Category> categories = categoryMapper.selectAll();
            recordOperationLog("分类管理", "查询分类列表", 1, null);
            return HttpResult.ok(categories);
        } catch (Exception e) {
            log.error("查询分类列表失败：", e);
            recordOperationLog("分类管理", "查询分类列表", 0, e.getMessage());
            return HttpResult.error("查询分类列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/category/save")
    @RequiresPermission(code = "category:manage")
    public HttpResult save(@RequestBody Category category) {
        try {
            if (category.getId() == null) {
                categoryMapper.insert(category);
                recordOperationLog("分类管理", "添加分类", 1, null);
            } else {
                categoryMapper.update(category);
                recordOperationLog("分类管理", "修改分类", 1, null);
            }
            return HttpResult.ok(category);
        } catch (Exception e) {
            log.error("保存分类失败：", e);
            recordOperationLog("分类管理", "保存分类", 0, e.getMessage());
            return HttpResult.error("保存分类失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/category/delete/{id}")
    @RequiresPermission(code = "category:manage")
    public HttpResult delete(@PathVariable Integer id) {
        try {
            categoryMapper.delete(id);
            recordOperationLog("分类管理", "删除分类", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除分类失败：", e);
            recordOperationLog("分类管理", "删除分类", 0, e.getMessage());
            return HttpResult.error("删除分类失败：" + e.getMessage());
        }
    }

    /**
     * 用户获取分类列表
     * @return 分类列表
     */
    @GetMapping("/category/list")
    public HttpResult userList() {
        List<Category> categories = categoryMapper.selectActive();
        return HttpResult.ok(categories);
    }
}
