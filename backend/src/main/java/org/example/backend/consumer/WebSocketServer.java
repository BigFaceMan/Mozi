package org.example.backend.consumer;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketServer extends TextWebSocketHandler {
    private static final CopyOnWriteArraySet<WebSocketSession> clients = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        clients.add(session);
        System.out.println("新连接: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        System.out.println("收到消息: " + message.getPayload());

        // 向所有客户端广播消息
        for (WebSocketSession client : clients) {
            if (client.isOpen()) {
                client.sendMessage(new TextMessage("服务器收到: " + message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clients.remove(session);
        System.out.println("连接关闭: " + session.getId());
    }
}
