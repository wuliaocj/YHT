package com.example.demo.controller;

import com.example.demo.domain.Coupon;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.User;
import com.example.demo.domain.UserCoupon;
import com.example.demo.http.HttpResult;
import com.example.demo.service.CouponService;
import com.example.demo.service.LogService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtil;
import com.example.demo.annotation.RequiresPermission;
import com.example.demo.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final UserService userService;
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

    // 管理员接口
    @GetMapping("/admin/list")
    @RequiresPermission(code = "coupon:read")
    public HttpResult adminList() {
        try {
            List<Coupon> list = couponService.listAll();
            recordOperationLog("优惠券管理", "查询优惠券列表", 1, null);
            return HttpResult.ok(list);
        } catch (Exception e) {
            log.error("查询优惠券列表失败：", e);
            recordOperationLog("优惠券管理", "查询优惠券列表", 0, e.getMessage());
            return HttpResult.error("查询优惠券列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/save")
    @RequiresPermission(code = "coupon:manage")
    public HttpResult adminSave(@RequestBody Coupon coupon) {
        try {
            boolean saved = couponService.save(coupon);
            if (coupon.getId() == null) {
                recordOperationLog("优惠券管理", "添加优惠券", 1, null);
            } else {
                recordOperationLog("优惠券管理", "修改优惠券", 1, null);
            }
            return HttpResult.ok(coupon);
        } catch (Exception e) {
            log.error("保存优惠券失败：", e);
            recordOperationLog("优惠券管理", "保存优惠券", 0, e.getMessage());
            return HttpResult.error("保存优惠券失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/delete/{id}")
    @RequiresPermission(code = "coupon:manage")
    public HttpResult adminDelete(@PathVariable Integer id) {
        try {
            couponService.delete(id);
            recordOperationLog("优惠券管理", "删除优惠券", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            log.error("删除优惠券失败：", e);
            recordOperationLog("优惠券管理", "删除优惠券", 0, e.getMessage());
            return HttpResult.error("删除优惠券失败：" + e.getMessage());
        }
    }

    /**
     * 获取可领取的优惠券列表
     * @return 可领取的优惠券列表
     */
    @GetMapping("/available")
    public HttpResult listAvailableCoupons() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<Coupon> list = couponService.listAvailableCoupons(userId);
        return HttpResult.ok(list);
    }

    /**
     * 获取我的优惠券列表
     * @param status 状态：0-未使用，1-已使用，2-已过期，null-全部
     * @return 用户优惠券列表
     */
    @GetMapping("/my")
    public HttpResult listMyCoupons(@RequestParam(required = false) Integer status) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<UserCouponVO> list = couponService.listUserCouponsWithDetails(userId);
        // 按状态筛选
        if (status != null) {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            list.removeIf(uc -> {
                boolean isExpired = uc.getExpireTime() != null && uc.getExpireTime().isBefore(now);
                if (status == 0) {
                    // 未使用：状态为0且未过期
                    return uc.getStatus() != 0 || isExpired;
                } else if (status == 1) {
                    // 已使用：状态为1
                    return uc.getStatus() != 1;
                } else if (status == 2) {
                    // 已过期：状态为0但已过期
                    return uc.getStatus() != 0 || !isExpired;
                }
                return false;
            });
        }
        return HttpResult.ok(list);
    }

    /**
     * 获取我的可用优惠券列表（未使用且未过期）
     * @return 可用优惠券列表
     */
    @GetMapping("/my/available")
    public HttpResult listMyAvailableCoupons() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<UserCoupon> list = couponService.listUserAvailableCoupons(userId);
        return HttpResult.ok(list);
    }

    /**
     * 领取优惠券
     * @param couponId 优惠券ID
     * @return 领取结果
     */
    @PostMapping("/receive")
    public HttpResult receiveCoupon(@RequestParam Integer couponId) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        try {
            couponService.receiveCoupon(userId, couponId);
            log.info("用户{}领取优惠券{}成功", userId, couponId);
            return HttpResult.ok("领取成功");
        } catch (Exception e) {
            log.warn("领取优惠券失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        }
    }

    /**
     * 使用优惠券码领取
     * @param couponCode 优惠券码
     * @return 领取结果
     */
    @PostMapping("/receiveByCode")
    public HttpResult receiveCouponByCode(@RequestParam String couponCode) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        try {
            UserCoupon userCoupon = couponService.getCouponByCode(couponCode);
            if (userCoupon == null) {
                return HttpResult.error("优惠券码无效");
            }
            couponService.receiveCoupon(userId, userCoupon.getCouponId());
            log.info("用户{}通过优惠券码领取优惠券{}成功", userId, userCoupon.getCouponId());
            return HttpResult.ok("领取成功");
        } catch (Exception e) {
            log.warn("领取优惠券失败：{}", e.getMessage());
            return HttpResult.error(e.getMessage());
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            String openid = (String) authentication.getPrincipal();
            if (openid == null) {
                return null;
            }

            User user = userService.getUserByOpenid(openid);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }
}
