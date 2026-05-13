package com.example.demo.controller;

import com.example.demo.domain.Banner;
import com.example.demo.domain.OperationLog;
import com.example.demo.http.HttpResult;
import com.example.demo.service.BannerService;
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
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;
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

    @GetMapping("/list")
    @RequiresPermission(code = "banner:read")
    public HttpResult list() {
        try {
            List<Banner> list = bannerService.listAll();
            recordOperationLog("轮播图管理", "查询轮播图列表", 1, null);
            return HttpResult.ok(list);
        } catch (Exception e) {
            log.error("查询轮播图列表失败：", e);
            recordOperationLog("轮播图管理", "查询轮播图列表", 0, e.getMessage());
            return HttpResult.error("查询轮播图列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/save")
    @RequiresPermission( code = "banner:manage")
    public HttpResult save(@RequestBody Banner banner) {
        try {
            boolean saved = bannerService.save(banner);
            if (banner.getId() == null) {
                recordOperationLog("轮播图管理", "添加轮播图", 1, null);
            } else {
                recordOperationLog("轮播图管理", "修改轮播图", 1, null);
            }
            return HttpResult.ok(banner);
        } catch (Exception e) {
            log.error("保存轮播图失败：", e);
            recordOperationLog("轮播图管理", "保存轮播图", 0, e.getMessage());
            return HttpResult.error("保存轮播图失败：" + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    @RequiresPermission( code = "banner:manage")
    public HttpResult delete(@PathVariable Integer id) {
        try {
            bannerService.delete(id);
            recordOperationLog("轮播图管理", "删除轮播图", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除轮播图失败：", e);
            recordOperationLog("轮播图管理", "删除轮播图", 0, e.getMessage());
            return HttpResult.error("删除轮播图失败：" + e.getMessage());
        }
    }

    /**
     * 用户获取轮播图列表
     * @return 轮播图列表
     */
    @GetMapping("/user/list")
    public HttpResult userList() {
        List<Banner> list = bannerService.listActive();
        return HttpResult.ok(list);
    }

}
