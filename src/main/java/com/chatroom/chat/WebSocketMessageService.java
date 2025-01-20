package com.chatroom.chat;

import com.chatroom.room.RoomService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketMessageService implements MessageService<WebSocketMessage> {

    @Getter
    private Map<String, List<WebSocketMessage>> messageHistoryMap = new ConcurrentHashMap<>();
    private final RoomService roomService;

    @Autowired
    public WebSocketMessageService(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public void createMessageListForRoom(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId) && !messageHistoryMap.containsKey(roomId)) {
            messageHistoryMap.put(roomId, new ArrayList<>());
        }
    }

    @Override
    public void saveMessage(String roomId, WebSocketMessage message) {
        if (roomService.getAllRooms().containsKey(roomId) && message.getSender() != null) {
            messageHistoryMap.get(roomId).add(message);
        }
    }

    @Override
    public List<WebSocketMessage> exportMessages(String roomId) {
        // Return the list of messages for this room, or empty list if none exist
        List<WebSocketMessage> history = messageHistoryMap.getOrDefault(roomId, Collections.emptyList());
        history.sort(Comparator.comparingLong(WebSocketMessage::getTimestamp));
        return history;
    }

    @Override
    public void deleteRoomMessages(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            messageHistoryMap.remove(roomId);
        }
    }

}
