package com.example.demo.controller;

import com.example.demo.http.HttpResult;
import com.example.demo.service.UserPointService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/point")
public class UserPointController {

    private final UserPointService userPointService;

    public UserPointController(UserPointService userPointService) {
        this.userPointService = userPointService;
    }

    /**
     * 获取用户积分余额
     * @param userId
     * @return
     */
    @GetMapping("/balance/{userId}")
    public HttpResult getPointBalance(@PathVariable Integer userId) {
        return HttpResult.ok(userPointService.getUserPointBalance(userId));
    }

    /**
     * 获取用户积分记录
     * @param userId
     * @return
     */
    @GetMapping("/records/{userId}")
    public HttpResult getPointRecords(@PathVariable Integer userId) {
        return HttpResult.ok(userPointService.getUserPointRecords(userId));
    }

    /**
     * 手动增加积分（管理员接口）
     * @param userId
     * @param point
     * @param source
     * @param remark
     * @return
     */
    @PostMapping("/add")
    public HttpResult addPoint(@RequestParam Integer userId, 
                               @RequestParam Integer point, 
                               @RequestParam String source, 
                               @RequestParam String remark) {
        try {
            userPointService.addPoint(userId, point, source, remark);
            return HttpResult.ok("积分增加成功");
        } catch (Exception e) {
            return HttpResult.error(e.getMessage());
        }
    }

    /**
     * 手动使用积分（管理员接口）
     * @param userId
     * @param point
     * @param source
     * @param remark
     * @return
     */
    @PostMapping("/use")
    public HttpResult usePoint(@RequestParam Integer userId, 
                               @RequestParam Integer point, 
                               @RequestParam String source, 
                               @RequestParam String remark) {
        try {
            userPointService.usePoint(userId, point, source, remark);
            return HttpResult.ok("积分使用成功");
        } catch (Exception e) {
            return HttpResult.error(e.getMessage());
        }
    }
}
