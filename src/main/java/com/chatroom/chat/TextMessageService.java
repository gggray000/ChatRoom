package com.chatroom.chat;

import com.chatroom.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TextMessageService {

    private final RoomService roomService;

    public List<TextMessage> messageList;

    public Map<String, List<TextMessage>> messageHistoryMap;

    @Autowired
    public TextMessageService(RoomService roomService) {
        this.roomService = roomService;
        this.messageList = new ArrayList<>();
        this.messageHistoryMap = new ConcurrentHashMap<>();
    }

    public void createMessageListForRoom(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            List<TextMessage> messagesListForRoom = new ArrayList<>();
            messageHistoryMap.put(roomId, messagesListForRoom);
        }
    }

//    public void saveTextMessage(TextMessage msg){
//        if (msg.getUserName() != null) {
//            messageList.add(msg);
//        }
//    }

    public void saveTextMessage(String roomId, TextMessage msg) {
        if (roomService.getAllRooms().containsKey(roomId) && msg.getUserName() != null) {
            messageHistoryMap.get(roomId).add(msg);
        }
    }

    public String exportStoredMessages(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            return messageHistoryMap.get(roomId).stream()
                    .map(textMessage -> textMessage.getUserName() + ": " + textMessage.getContent() + "\n")
                    .collect(Collectors.joining());
//            return new ArrayList<>(messageList).stream()
//                    .filter(textMessage -> textMessage.getRoomId().equals(roomId))
//                    .map(textMessage -> textMessage.getUserName() + ": " + textMessage.getContent() + "\n")
//                    .collect(Collectors.joining());
        }
        return "";
    }

    public void deleteRoomMessageHistory(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            messageHistoryMap.remove(roomId);
            //messageList.removeIf(textMessage -> textMessage.getRoomId().equals(roomId));
        }
    }
}
