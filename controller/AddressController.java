package com.example.demo.controller;

import com.example.demo.domain.Address;
import com.example.demo.domain.User;
import com.example.demo.http.HttpResult;
import com.example.demo.service.AddressService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    /**
     * 获取当前用户的收货地址列表
     * @return 收货地址列表
     */
    @GetMapping("/list")
    public HttpResult list() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        List<Address> list = addressService.listByUser(userId);
        log.info("用户{}获取收货地址列表成功，地址数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 根据用户ID获取收货地址列表（管理员接口）
     * @param userId 用户ID
     * @return 收货地址列表
     */
    @GetMapping("/list/{userId}")
    public HttpResult listByUserId(@PathVariable Integer userId) {
        List<Address> list = addressService.listByUser(userId);
        log.info("管理员获取用户{}收货地址列表成功，地址数量：{}", userId, list.size());
        return HttpResult.ok(list);
    }

    /**
     * 保存收货地址
     * @param address 收货地址信息
     * @return 保存结果
     */
    @PostMapping("/save")
    public HttpResult save(@RequestBody Address address) {
        // 强制从JWT获取当前登录用户ID，禁止从请求体传入
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录，请先登录");
        }

        // 参数验证
        if (address.getConsignee() == null || address.getConsignee().trim().isEmpty()) {
            return HttpResult.error("收货人姓名不能为空");
        }
        if (address.getPhone() == null || address.getPhone().trim().isEmpty()) {
            return HttpResult.error("手机号不能为空");
        }
        if (address.getProvince() == null || address.getProvince().trim().isEmpty()) {
            return HttpResult.error("省份不能为空");
        }
        if (address.getCity() == null || address.getCity().trim().isEmpty()) {
            return HttpResult.error("城市不能为空");
        }
        if (address.getDistrict() == null || address.getDistrict().trim().isEmpty()) {
            return HttpResult.error("区县不能为空");
        }
        if (address.getDetailAddress() == null || address.getDetailAddress().trim().isEmpty()) {
            return HttpResult.error("详细地址不能为空");
        }

        // 设置当前用户ID
        address.setUserId(userId);

        Address saved = addressService.save(address);
        log.info("用户{}保存收货地址成功，地址ID：{}", userId, saved.getId());
        return HttpResult.ok(saved);
    }

    /**
     * 删除收货地址
     * @param id 地址ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    public HttpResult delete(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录，请先登录");
        }
        Address address = addressService.getById(id);
        if (address == null) {
            return HttpResult.error("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            return HttpResult.error("无权删除该地址");
        }
        addressService.delete(id);
        log.info("用户{}删除收货地址成功，地址ID：{}", userId, id);
        return HttpResult.ok("删除成功");
    }

    /**
     * 设置默认收货地址
     * @param id 地址ID
     * @return 设置结果
     */
    @PostMapping("/default/{id}")
    public HttpResult setDefault(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录，请先登录");
        }
        Address address = addressService.getById(id);
        if (address == null) {
            return HttpResult.error("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            return HttpResult.error("无权操作该地址");
        }
        addressService.setDefault(userId, id);
        log.info("用户{}设置默认收货地址成功，地址ID：{}", userId, id);
        return HttpResult.ok("设置成功");
    }

    /**
     * 获取默认收货地址
     * @return 默认收货地址
     */
    @GetMapping("/default")
    public HttpResult getDefault() {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        Address address = addressService.getDefaultByUserId(userId);
        return HttpResult.ok(address);
    }

    /**
     * 获取收货地址详情
     * @param id 地址ID
     * @return 地址详情
     */
    @GetMapping("/{id}")
    public HttpResult getById(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录");
        }
        Address address = addressService.getById(id);
        if (address == null) {
            return HttpResult.error("地址不存在");
        }
        if (!userId.equals(address.getUserId())) {
            return HttpResult.error("无权查看该地址");
        }
        return HttpResult.ok(address);
    }

    /**
     * 更新收货地址
     * @param id 地址ID
     * @param address 地址信息
     * @return 更新结果
     */
    @PostMapping("/update/{id}")
    public HttpResult update(@PathVariable Integer id, @RequestBody Address address) {
        Integer userId = getCurrentUserId();
        if (userId == null) {
            return HttpResult.error("用户未登录，请先登录");
        }
        Address existAddress = addressService.getById(id);
        if (existAddress == null) {
            return HttpResult.error("地址不存在");
        }
        if (!userId.equals(existAddress.getUserId())) {
            return HttpResult.error("无权修改该地址");
        }
        address.setId(id);
        address.setUserId(userId);
        addressService.update(address);
        log.info("用户{}更新收货地址成功，地址ID：{}", userId, id);
        return HttpResult.ok("更新成功");
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
            log.info("获取当前用户ID成功：{}", user.getId());
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            log.warn("获取当前用户ID失败：", e);
            return null;
        }
    }
}
