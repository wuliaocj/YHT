//package com.example.demo.config;
//
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//@RestController
//@RequestMapping("/api/proxy")
//public class ProxyController {
//    private final RestTemplate restTemplate = new RestTemplate();
//    // 目标接口基础地址
//    private final String TARGET_BASE_URL = "https://48523soty654.vicp.fun/api/admin";
//
//    // 代理用户列表接口
//    @GetMapping("/user/list")
//    public ResponseEntity<?> proxyUserList() {
//        String targetUrl = TARGET_BASE_URL + "/user/list";
//        // 转发请求并返回结果
//        return restTemplate.exchange(targetUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Object.class);
//    }
//
//    // 代理优惠券列表接口
//    @GetMapping("/coupon/list")
//    public ResponseEntity<?> proxyCouponList() {
//        String targetUrl = TARGET_BASE_URL + "/coupon/list";
//        return restTemplate.exchange(targetUrl, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), Object.class);
//    }
//}
