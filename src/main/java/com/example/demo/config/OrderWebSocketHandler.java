package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单WebSocket处理器
 */
@Slf4j
public class OrderWebSocketHandler extends TextWebSocketHandler {

    // 存储用户ID和WebSocket会话的映射
    private static final ConcurrentHashMap<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 连接建立时触发
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话中获取用户ID（实际项目中可能需要从token或其他方式获取）
        // 这里简化处理，假设用户ID通过查询参数传递
        String userIdStr = session.getUri().getQuery().split("=")[1];
        Integer userId = Integer.parseInt(userIdStr);
        
        // 存储用户会话
        userSessions.put(userId, session);
        log.info("用户{}建立WebSocket连接，当前连接数：{}", userId, userSessions.size());
        
        // 发送连接成功消息
        session.sendMessage(new TextMessage("{\"type\":\"connect\",\"message\":\"连接成功\"}"));
    }

    /**
     * 接收消息时触发
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("接收到消息：{}", message.getPayload());
        // 这里可以处理客户端发送的消息
        // 例如心跳检测、订阅特定订单等
    }

    /**
     * 连接关闭时触发
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除用户会话
        userSessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.info("用户WebSocket连接关闭，当前连接数：{}", userSessions.size());
    }

    /**
     * 发送订单状态通知
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     * @param message 通知消息
     */
    public static void sendOrderStatusNotification(Integer userId, Integer orderId, Integer status, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String notification = String.format(
                        "{\"type\":\"order_status\",\"orderId\":%d,\"status\":%d,\"message\":\"%s\"}",
                        orderId, status, message
                );
                session.sendMessage(new TextMessage(notification));
                log.info("发送订单状态通知给用户{}，订单ID：{}，状态：{}", userId, orderId, status);
            } catch (IOException e) {
                log.error("发送订单状态通知失败：", e);
            }
        } else {
            log.warn("用户{}未建立WebSocket连接，无法发送订单状态通知", userId);
        }
    }

    /**
     * 获取当前连接数
     * @return 连接数
     */
    public static int getConnectionCount() {
        return userSessions.size();
    }
}
