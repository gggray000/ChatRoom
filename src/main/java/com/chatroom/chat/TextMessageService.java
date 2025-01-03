package com.chatroom.chat;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextMessageService {

    public List<TextMessage> messageList;

    public TextMessageService() {
        this.messageList = new ArrayList<>();
    }

    public void saveTextMessage(TextMessage msg){
        messageList.add(msg);
    }

    public String exportStoredMessages(String roomId) {
        return new ArrayList<>(messageList).stream()
                .filter(textMessage -> textMessage.getRoomId().equals(roomId))
                .map(textMessage -> textMessage.getUserName() + ": " + textMessage.getContent() + "\n")
                .collect(Collectors.joining());
    }

}
