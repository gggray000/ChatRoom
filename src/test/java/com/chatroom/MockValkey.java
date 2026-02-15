package com.chatroom;

import com.chatroom.cache.MessageStore;
import com.chatroom.chat.WebSocketMessage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MockValkey implements MessageStore {
    private final Map<String, List<WebSocketMessage>> map = new HashMap<>();

    public void ensureRoomStreamExists(String roomId, Duration ttl) {
        map.computeIfAbsent(roomId, k -> new ArrayList<>());
    }

    public void append(String roomId, WebSocketMessage msg, Duration ttl, long approxMaxLen) {
        map.computeIfAbsent(roomId, k -> new ArrayList<>()).add(msg);
        // approximate trim
        List<WebSocketMessage> list = map.get(roomId);
        if (list.size() > approxMaxLen) {
            int from = (int) Math.max(0, list.size() - approxMaxLen);
            map.put(roomId, new ArrayList<>(list.subList(from, list.size())));
        }
    }

    public List<WebSocketMessage> readAllRetained(String roomId) {
        List<WebSocketMessage> list = map.getOrDefault(roomId, List.of());
        return new ArrayList<>(list.subList(0, list.size()));
    }

    public void deleteRoom(String roomId) {
        map.remove(roomId);
    }
}