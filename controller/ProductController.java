package com.example.demo.controller;

import com.example.demo.domain.OperationLog;
import com.example.demo.http.HttpResult;
import com.example.demo.service.LogService;
import com.example.demo.service.ProductService;
import com.example.demo.util.JwtUtil;
import com.example.demo.vo.AddProductVO;
import com.example.demo.vo.GetProductVO;
import com.example.demo.vo.PageRequestVO;
import com.example.demo.vo.PageResponseVO;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.example.demo.annotation.RequiresPermission;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
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

    @GetMapping("/detail/{id}")
    public HttpResult detail(@PathVariable(required = false) Long id) {
        if (id == null) {
            return HttpResult.error("商品ID不能为空");
        }
        try {
            GetProductVO productVO = productService.getProductById(id);
            log.info("查询商品详情成功，商品ID：{}", id);
            return HttpResult.ok("查询成功",productVO);
        } catch (Exception e) {
            log.error("查询商品详情失败：", e);
            return HttpResult.error("查询商品详情失败：" + e.getMessage());
        }
    }

    /**
     * 添加商品，同时包括了商品可用的小料和规格
     * @param addProductVO 商品信息
     * @return 添加结果
     */
    @PostMapping("/admin/add")
    @RequiresPermission(code = "product:manage")
    public HttpResult addProduct(@Valid @RequestBody AddProductVO addProductVO) {
        try {
            String result = productService.addProduct(addProductVO);
            log.info("添加商品结果：{}", result);
            recordOperationLog("商品管理", "添加商品", 1, null);
            return HttpResult.ok("商品添加成功");
        } catch (Exception e) {
            log.error("添加商品失败：", e);
            recordOperationLog("商品管理", "添加商品", 0, e.getMessage());
            return HttpResult.error("添加商品失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    @RequiresPermission(code = "product:read")
    public HttpResult listProduct(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer status) {
        try {
            PageRequestVO pageRequest = new PageRequestVO();
            pageRequest.setPageNum(page);
            pageRequest.setPageSize(pageSize);
            PageResponseVO<GetProductVO> result = productService.getProductListByPage(pageRequest, keyword, categoryId, status);
            recordOperationLog("商品管理", "查询商品列表", 1, null);
            return HttpResult.ok("商品查询成功", result);
        } catch (Exception e) {
            log.error("查询商品列表失败：", e);
            recordOperationLog("商品管理", "查询商品列表", 0, e.getMessage());
            return HttpResult.error("查询商品列表失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/update")
    @RequiresPermission(code = "product:manage")
    public HttpResult updateProduct(@Valid @RequestBody AddProductVO addProductVO) {
        try {
            var result = productService.updateProduct(addProductVO);
            recordOperationLog("商品管理", "修改商品", 1, null);
            return HttpResult.ok("商品修改成功", result);
        } catch (Exception e) {
            log.error("修改商品失败：", e);
            recordOperationLog("商品管理", "修改商品", 0, e.getMessage());
            return HttpResult.error("修改商品失败：" + e.getMessage());
        }
    }

    @GetMapping("/user/list")
    public HttpResult listUserProduct() {
        log.info("用户商品查询成功");
        return HttpResult.ok("用户商品查询成功",productService.getProductList());
    }

    /**
     * 获取推荐商品列表（仅显示管理员设置了推荐的商品）
     * @return 推荐商品列表
     */
    @GetMapping("/user/recommend")
    public HttpResult listRecommendProducts() {
        log.info("查询推荐商品列表");
        return HttpResult.ok("查询成功", productService.listRecommendProducts());
    }

    /**
     * 搜索商品
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/user/search")
    public HttpResult searchProduct(@RequestParam String keyword) {
        try {
            log.info("商品搜索关键词：{}", keyword);
            return HttpResult.ok("商品搜索成功",productService.searchProducts(keyword));
        } catch (Exception e) {
            log.error("搜索商品失败：", e);
            return HttpResult.error("搜索商品失败：" + e.getMessage());
        }
    }

    @PostMapping("/admin/delete/{id}")
    @RequiresPermission(code = "product:manage")
    public HttpResult deleteProduct(@PathVariable(required = false) Long id) {
        if (id == null) {
            recordOperationLog("商品管理", "删除商品", 0, "商品ID不能为空");
            return HttpResult.error("商品ID不能为空");
        }
        try {
            productService.deleteProduct(id);
            recordOperationLog("商品管理", "删除商品", 1, null);
            return HttpResult.ok("商品删除成功");
        } catch (Exception e) {
            log.error("删除商品失败：", e);
            recordOperationLog("商品管理", "删除商品", 0, e.getMessage());
            return HttpResult.error("删除商品失败：" + e.getMessage());
        }
    }

}


