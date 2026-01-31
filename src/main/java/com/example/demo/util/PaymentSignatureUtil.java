package com.example.demo.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

/**
 * 支付签名验证工具类
 */
@Slf4j
@Component
public class PaymentSignatureUtil {

    /**
     * 验证微信支付回调签名
     * @param callbackData 回调数据
     * @param apiKey 微信支付密钥
     * @return true-签名验证通过，false-签名验证失败
     */
    public boolean verifyWxPaySignature(Map<String, String> callbackData, String apiKey) {
        try {
            String sign = callbackData.get("sign");
            if (sign == null || sign.isEmpty()) {
                log.error("微信支付回调签名缺失");
                return false;
            }

            // 构建待签名字符串
            String signString = buildWxPaySignString(callbackData);
            
            // 计算MD5签名
            String calculatedSign = md5(signString + "&key=" + apiKey).toUpperCase();
            
            boolean result = sign.equals(calculatedSign);
            if (!result) {
                log.error("微信支付签名验证失败，期望签名：{}，实际签名：{}", calculatedSign, sign);
            }
            
            return result;
        } catch (Exception e) {
            log.error("微信支付签名验证异常", e);
            return false;
        }
    }

    /**
     * 验证支付宝回调签名
     * @param callbackData 回调数据
     * @param alipayPublicKey 支付宝公钥
     * @return true-签名验证通过，false-签名验证失败
     */
    public boolean verifyAlipaySignature(Map<String, String> callbackData, String alipayPublicKey) {
        try {
            String sign = callbackData.get("sign");
            String signType = callbackData.get("sign_type");
            
            if (sign == null || sign.isEmpty()) {
                log.error("支付宝回调签名缺失");
                return false;
            }

            if (alipayPublicKey == null || alipayPublicKey.isEmpty()) {
                log.error("支付宝公钥未配置");
                return false;
            }

            // 构建待签名字符串
            String signString = buildAlipaySignString(callbackData);
            
            // 验证签名
            boolean result = verifySignature(signString, sign, alipayPublicKey, signType);
            if (!result) {
                log.error("支付宝签名验证失败");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("支付宝签名验证异常", e);
            return false;
        }
    }

    /**
     * 验证签名
     * @param data 待签名数据
     * @param sign 签名
     * @param publicKey 公钥
     * @param signType 签名类型
     * @return true-签名验证通过，false-签名验证失败
     */
    private boolean verifySignature(String data, String sign, String publicKey, String signType) {
        try {
            // 处理公钥格式
            publicKey = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            // 解码Base64签名
            byte[] signBytes = java.util.Base64.getDecoder().decode(sign);

            // 获取签名算法
            java.security.Signature signature;
            if ("RSA2".equals(signType)) {
                signature = java.security.Signature.getInstance("SHA256withRSA");
            } else {
                signature = java.security.Signature.getInstance("SHA1withRSA");
            }

            // 加载公钥
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(
                    java.util.Base64.getDecoder().decode(publicKey)
            );
            java.security.PublicKey pubKey = keyFactory.generatePublic(keySpec);

            // 初始化签名并验证
            signature.initVerify(pubKey);
            signature.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return signature.verify(signBytes);

        } catch (Exception e) {
            log.error("签名验证异常", e);
            return false;
        }
    }

    /**
     * 构建微信支付待签名字符串
     * @param params 参数Map
     * @return 待签名字符串
     */
    private String buildWxPaySignString(Map<String, String> params) {
        // 过滤空值和sign字段
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key != null && !key.isEmpty() && 
                value != null && !value.isEmpty() && 
                !"sign".equals(key)) {
                sortedParams.put(key, value);
            }
        }

        // 按字典序排序并拼接
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return sb.toString();
    }

    /**
     * 构建支付宝待签名字符串
     * @param params 参数Map
     * @return 待签名字符串
     */
    private String buildAlipaySignString(Map<String, String> params) {
        // 过滤空值和sign字段
        TreeMap<String, String> sortedParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            if (key != null && !key.isEmpty() && 
                value != null && !value.isEmpty() && 
                !"sign".equals(key) && !"sign_type".equals(key)) {
                sortedParams.put(key, value);
            }
        }

        // 按字典序排序并拼接
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return sb.toString();
    }

    /**
     * MD5加密
     * @param text 明文
     * @return MD5密文
     */
    private String md5(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("MD5加密失败", e);
            return "";
        }
    }

    /**
     * HMAC-SHA256加密
     * @param text 明文
     * @param key 密钥
     * @return HMAC-SHA256密文
     */
    public String hmacSha256(String text, String key) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKey);
            byte[] digest = hmac.doFinal(text.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("HMAC-SHA256加密失败", e);
            return "";
        }
    }
}