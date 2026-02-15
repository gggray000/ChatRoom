package com.chatroom.cache;

import com.chatroom.chat.WebSocketMessage;

import java.time.Duration;
import java.util.List;

public interface MessageStore {
    void ensureRoomStreamExists(String roomId, Duration ttl);

    void append(String roomId, WebSocketMessage msg, Duration ttl, long approxMaxLen);

    List<WebSocketMessage> readAllRetained(String roomId);

    void deleteRoom(String roomId);
}
