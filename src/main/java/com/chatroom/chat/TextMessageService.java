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
import java.util.stream.Collectors;

@Service
public class TextMessageService implements MessageService<TextMessage> {

    @Getter
    private Map<String, List<TextMessage>> messageHistoryMap;
    private final RoomService roomService;

    @Autowired
    public TextMessageService(RoomService roomService) {
        this.roomService = roomService;
        this.messageHistoryMap = new ConcurrentHashMap<>();
    }

    public void createMessageListForRoom(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            List<TextMessage> messagesListForRoom = new ArrayList<>();
            messageHistoryMap.put(roomId, messagesListForRoom);
        }
    }

    @Override
    public void saveMessage(String roomId, TextMessage message) {
        if (roomService.getAllRooms().containsKey(roomId) && message.getSender() != null) {
            messageHistoryMap.get(roomId).add(message);
        }
    }

    @Override
    public List<TextMessage> exportMessages(String roomId) {
        return messageHistoryMap.getOrDefault(roomId, Collections.emptyList());
    }

    public String messageHistoryToString(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            return exportMessages(roomId).stream()
                    .map(textMessage -> textMessage.getSender() + ": " + textMessage.getContent() + "\n")
                    .collect(Collectors.joining());
        }
        return "";
    }

    @Override
    public void deleteRoomMessages(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            messageHistoryMap.remove(roomId);
        }
    }
}
