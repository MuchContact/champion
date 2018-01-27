package com.egova.websocket.jetty.echo;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EchoRepository {

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    public static void addWebSocketSession(WebSocketSession session) {
        sessions.add(session);
    }

    public static void removeWebSocketSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public static void broadcast(TextMessage textMessage) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(textMessage);
        }

    }
}
