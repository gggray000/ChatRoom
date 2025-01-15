package com.chatroom.chat;

import com.chatroom.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextMessageService {

    private final RoomService roomService;

    public List<TextMessage> messageList;

    @Autowired
    public TextMessageService(RoomService roomService) {
        this.roomService = roomService;
        this.messageList = new ArrayList<>();
    }

    public void saveTextMessage(TextMessage msg){
        if (msg.getUserName() != null) {
            messageList.add(msg);
        }
    }

    public String exportStoredMessages(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            return new ArrayList<>(messageList).stream()
                    .filter(textMessage -> textMessage.getRoomId().equals(roomId))
                    .map(textMessage -> textMessage.getUserName() + ": " + textMessage.getContent() + "\n")
                    .collect(Collectors.joining());
        }
        return "";
    }

    public void deleteRoomMessageHistory(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            messageList.removeIf(textMessage -> textMessage.getRoomId().equals(roomId));
        }
    }
}
