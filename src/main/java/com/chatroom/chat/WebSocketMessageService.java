package com.chatroom.chat;

import com.chatroom.room.RoomService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketMessageService {

    @Getter
    private Map<String, List<WebSocketMessage>> webSocketMessageMap = new ConcurrentHashMap<>();
    private final RoomService roomService;

    @Autowired
    public WebSocketMessageService(RoomService roomService) {
        this.roomService = roomService;
    }

    public void createWebSocketListForRoom(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId) && !webSocketMessageMap.containsKey(roomId)) {
            webSocketMessageMap.put(roomId, new ArrayList<>());
        }
    }

    public void saveWebSocketMessage(String roomId, WebSocketMessage webSocketMessage) {
        if (roomService.getRoom(roomId) == null) {
            return;
        }
        if (webSocketMessage.getSender() != null) {
            List<WebSocketMessage> messages = webSocketMessageMap.get(roomId);
            messages.add(webSocketMessage);
        }
    }

    public List<WebSocketMessage> exportWebSocketMessage(String roomId) {
        // Return the list of messages for this room, or empty list if none exist
        return webSocketMessageMap.getOrDefault(roomId, Collections.emptyList());
    }

    public void deleteRoomWebSocketMessageHistory(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            webSocketMessageMap.remove(roomId);
            //messageList.removeIf(textMessage -> textMessage.getRoomId().equals(roomId));
        }
    }

}
