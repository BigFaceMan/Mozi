package org.example.backend.consumer;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Scanner;

@ClientEndpoint
@Component
public class WebSocketClient {
    private Session session;

    public void connect(String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("WebSocket 连接已建立！");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("收到服务器消息: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket 连接关闭: " + reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误: " + throwable.getMessage());
    }

    public void sendMessage(String message) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(message);
        } else {
            System.out.println("连接已关闭，无法发送消息！");
        }
    }

    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
