package com.example.demo.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单WebSocket处理器
 */
public class OrderWebSocketHandler extends TextWebSocketHandler {

    // 存储用户会话，key为userId
    private static final ConcurrentHashMap<Integer, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话中获取用户ID（实际项目中可能需要从token或其他方式获取）
        // 这里简化处理，假设前端会发送用户ID
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理前端发送的消息，例如用户ID绑定
        String payload = message.getPayload();
        try {
            // 假设前端发送的是用户ID
            Integer userId = Integer.parseInt(payload);
            userSessions.put(userId, session);
            // 发送连接成功消息
            session.sendMessage(new TextMessage("{\"type\":\"connect\",\"message\":\"连接成功\"}"));
        } catch (NumberFormatException e) {
            // 处理错误
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"无效的用户ID\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除断开连接的会话
        userSessions.values().removeIf(session1 -> session1.equals(session));
        super.afterConnectionClosed(session, status);
    }

    /**
     * 向指定用户发送订单状态通知
     * @param userId 用户ID
     * @param orderId 订单ID
     * @param status 订单状态
     * @param message 通知消息
     */
    public static void sendOrderNotification(Integer userId, Integer orderId, String status, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String notification = String.format(
                        "{\"type\":\"order_status\",\"orderId\":%d,\"status\":\"%s\",\"message\":\"%s\"}",
                        orderId, status, message
                );
                session.sendMessage(new TextMessage(notification));
            } catch (IOException e) {
                // 处理发送失败的情况
                e.printStackTrace();
            }
        }
    }
}
