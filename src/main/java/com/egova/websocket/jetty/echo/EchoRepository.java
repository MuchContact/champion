package com.egova.websocket.jetty.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EchoRepository {
    private static Logger logger = LoggerFactory.getLogger(EchoWebSocketHandler.class);

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<Integer, EchoMessage> messages = new ConcurrentHashMap<>();

    public static void addWebSocketSession(WebSocketSession session) {
        sessions.add(session);
        String s = null;
        try {
            TextMessage webSocketMessage = historyMessages();
            assert webSocketMessage != null;
            s = webSocketMessage.getPayload();
            session.sendMessage(webSocketMessage);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(String.format("向客户端发送消息失败（消息体：%s）", s));
        }
    }

    private static TextMessage historyMessages() {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<Integer, EchoMessage>> iterator = messages.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<Integer, EchoMessage> next = iterator.next();
            sb.append(next.getValue()).append(";");
        }

        return new TextMessage(sb.toString());
    }

    public static void removeWebSocketSession(WebSocketSession session) {
        sessions.remove(session);
    }

    public static void broadcast(TextMessage textMessage) throws IOException {
        for (WebSocketSession session : sessions) {
            session.sendMessage(textMessage);
        }

    }

    public static EchoMessage pushEchoMessage(String body) {
        String message = Objects.requireNonNull(body);
        String[] split = message.split(",");
        assert split.length > 1;

        EchoMessage echoMessage = new EchoMessage(Integer.valueOf(split[0]), split[1]);
        messages.put(echoMessage.getRound(), echoMessage);
        return echoMessage;
    }

    public static void reset() {
        messages.clear();
    }
}
