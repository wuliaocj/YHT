package com.example.demo.util;

import cn.hutool.json.JSONUtil;
import com.example.demo.vo.WxCode2SessionVO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 微信登录工具类
 */
@Component
public class WxLoginUtil {
    @Value("${wx.miniapp.appid}")
    private String appid;

    @Value("${wx.miniapp.secret}")
    private String secret;

    /**
     * 调用微信code2Session接口，获取openid
     * @param code 前端传的临时code
     * @return WxCode2SessionVO
     */
    public WxCode2SessionVO getOpenidByCode(String code) {
        // 构建请求URL
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appid, secret, code
        );

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                // 解析JSON为VO
                return JSONUtil.toBean(json, WxCode2SessionVO.class);
            }
        } catch (IOException e) {
            throw new RuntimeException("调用微信接口失败：" + e.getMessage());
        }
        return null;
    }
}
