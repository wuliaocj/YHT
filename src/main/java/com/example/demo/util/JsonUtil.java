package com.example.demo.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * JSON 工具类
 * 封装 ObjectMapper，提供全局唯一实例，简化 JSON 与 Java 对象的转换
 */
public class JsonUtil {

    // 全局唯一的 ObjectMapper 实例（线程安全）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 静态代码块：初始化 ObjectMapper 配置
    static {
        // 注册 JDK8 时间模块，支持 LocalDateTime/LocalDate 等类型的序列化/反序列化
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 忽略 JSON 字符串中存在但 Java 对象中不存在的字段（避免解析时报错）
        OBJECT_MAPPER.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许序列化空的 POJO（避免空对象序列化报错）
        OBJECT_MAPPER.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 配置序列化规则：去除空格、统一字段顺序、null值正常序列化
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, false); // 关闭格式化（无空格）
        OBJECT_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true); // 按字段名排序
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS); // 包含null值
    }

    // 私有化构造方法，禁止外部实例化
    private JsonUtil() {}

    /**
     * 将 Java 对象转换为 JSON 字符串
     * @param obj 待转换的对象（如 Map、List、实体类等）
     * @return JSON 字符串
     * @throws RuntimeException 转换失败时抛出运行时异常
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            // 包装为运行时异常，避免业务代码中频繁捕获检查型异常
            throw new RuntimeException("Java 对象转换为 JSON 字符串失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为指定类型的 Java 对象
     * @param json JSON 字符串
     * @param clazz 目标对象类型
     * @param <T> 泛型，适配任意类型
     * @return 转换后的 Java 对象
     * @throws RuntimeException 转换失败时抛出运行时异常
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 字符串转换为 Java 对象失败", e);
        }
    }

    /**
     * 将 JSON 字符串转换为复杂类型（如 List<Map>、Map<String, Object> 等）
     * @param json JSON 字符串
     * @param typeReference 复杂类型引用（如 new TypeReference<Map<String, Object>>() {}）
     * @param <T> 泛型
     * @return 转换后的复杂类型对象
     */
    public static <T> T fromJson(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 字符串转换为复杂类型对象失败", e);
        }
    }
}
