package com.example.demo.controller;

import com.example.demo.annotation.RequiresPermission;
import com.example.demo.domain.OperationLog;
import com.example.demo.domain.User;
import com.example.demo.domain.VipLevel;
import com.example.demo.http.HttpResult;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.LogService;
import com.example.demo.service.VipLevelService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vip")
@RequiredArgsConstructor
public class VipController {

    @Autowired
    private VipLevelService vipLevelService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LogService logService;

    private final JwtUtil jwtUtil;

    private Integer getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null) {
            return null;
        }
        try {
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private void recordOperationLog(String module, String operation, Integer status, String errorMsg) {
        try {
            Integer adminId = null;
            OperationLog operationLog = new OperationLog();
            operationLog.setModule(module);
            operationLog.setOperation(operation);
            operationLog.setStatus(status);
            operationLog.setErrorMessage(errorMsg);
            operationLog.setAdminId(adminId);
            logService.recordOperationLog(operationLog);
        } catch (Exception e) {
            VipController.log.error("记录操作日志失败：{}", e.getMessage());
        }
    }

    @GetMapping("/levels")
    public HttpResult getVipLevels() {
        try {
            List<VipLevel> levels = vipLevelService.getAllVipLevels();
            return HttpResult.ok(levels);
        } catch (Exception e) {
            log.error("获取VIP等级列表失败：", e);
            return HttpResult.error("获取VIP等级列表失败");
        }
    }

    @GetMapping("/current")
    public HttpResult getCurrentVip(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return HttpResult.error("用户未登录");
            }

            User user = userMapper.selectById(userId);
            if (user == null) {
                return HttpResult.error("用户不存在");
            }

            BigDecimal totalAmount = user.getTotalConsumption();
            if (totalAmount == null) {
                totalAmount = BigDecimal.ZERO;
            }

            VipLevel currentLevel = vipLevelService.getVipLevelByAmount(totalAmount);
            List<VipLevel> allLevels = vipLevelService.getAllVipLevels();

            Map<String, Object> result = new HashMap<>();
            
            if (currentLevel != null) {
                result.put("levelName", currentLevel.getName());
                result.put("levelIcon", currentLevel.getIcon());
                // 返回原始折扣率（用于计算）
                result.put("discountRate", currentLevel.getDiscountRate().doubleValue());
                // 返回显示用的折扣文本（如 0.92 -> 9.2折）
                double displayDiscount = currentLevel.getDiscountRate().multiply(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                result.put("discountText", String.format("%.1f折", displayDiscount));
            } else {
                result.put("levelName", "普通会员");
                result.put("levelIcon", "⭐");
                result.put("discountRate", 1.0);
                result.put("discountText", "10折");
            }
            
            // 累计消费保留两位小数，返回double类型
            result.put("totalConsumption", totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

            if (currentLevel != null && allLevels.size() > 0) {
                for (int i = 0; i < allLevels.size(); i++) {
                    VipLevel level = allLevels.get(i);
                    if (level.getId().equals(currentLevel.getId()) && i < allLevels.size() - 1) {
                        VipLevel nextLevel = allLevels.get(i + 1);
                        result.put("hasNextLevel", true);
                        result.put("nextLevelName", nextLevel.getName());
                        result.put("nextLevelIcon", nextLevel.getIcon());
                        // 返回double类型
                        result.put("nextLevelAmount", nextLevel.getMinAmount().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        BigDecimal needAmount = nextLevel.getMinAmount().subtract(totalAmount);
                        result.put("needAmount", needAmount.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                        break;
                    }
                }
            }

            return HttpResult.ok(result);
        } catch (Exception e) {
            log.error("获取VIP信息失败：", e);
            return HttpResult.error("获取VIP信息失败");
        }
    }

    @GetMapping("/admin/levels")
    @RequiresPermission(code = "vip:read")
    public HttpResult getAdminVipLevels() {
        try {
            List<VipLevel> levels = vipLevelService.getAllVipLevels();
            recordOperationLog("VIP管理", "查询VIP等级列表", 1, null);
            return HttpResult.ok(levels);
        } catch (Exception e) {
            recordOperationLog("VIP管理", "查询VIP等级列表", 0, e.getMessage());
            log.error("获取VIP等级列表失败：", e);
            return HttpResult.error("获取VIP等级列表失败");
        }
    }

    @PutMapping("/admin/levels/{id}")
    @RequiresPermission(code = "vip:manage")
    public HttpResult updateVipLevel(@PathVariable Integer id, @RequestBody VipLevel vipLevel) {
        try {
            vipLevel.setId(id);
            vipLevelService.updateById(vipLevel);
            log.info("更新VIP等级成功，ID：{}", id);
            recordOperationLog("VIP管理", "更新VIP等级", 1, null);
            return HttpResult.ok("更新成功");
        } catch (Exception e) {
            recordOperationLog("VIP管理", "更新VIP等级", 0, e.getMessage());
            log.error("更新VIP等级失败：", e);
            return HttpResult.error("更新失败");
        }
    }

    @PostMapping("/admin/levels")
    @RequiresPermission(code = "vip:manage")
    public HttpResult addVipLevel(@RequestBody VipLevel vipLevel) {
        try {
            vipLevelService.save(vipLevel);
            log.info("新增VIP等级成功，名称：{}", vipLevel.getName());
            recordOperationLog("VIP管理", "新增VIP等级", 1, null);
            return HttpResult.ok("新增成功");
        } catch (Exception e) {
            recordOperationLog("VIP管理", "新增VIP等级", 0, e.getMessage());
            log.error("新增VIP等级失败：", e);
            return HttpResult.error("新增失败");
        }
    }

    @DeleteMapping("/admin/levels/{id}")
    @RequiresPermission(code = "vip:manage")
    public HttpResult deleteVipLevel(@PathVariable Integer id) {
        try {
            vipLevelService.removeById(id);
            log.info("删除VIP等级成功，ID：{}", id);
            recordOperationLog("VIP管理", "删除VIP等级", 1, null);
            return HttpResult.ok("删除成功");
        } catch (Exception e) {
            recordOperationLog("VIP管理", "删除VIP等级", 0, e.getMessage());
            log.error("删除VIP等级失败：", e);
            return HttpResult.error("删除失败");
        }
    }
}
