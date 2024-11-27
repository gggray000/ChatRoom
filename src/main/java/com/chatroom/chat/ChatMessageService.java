package com.chatroom.chat;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatMessageService {

    public List<TextChatMessage> messageList = new ArrayList<>();

    public void saveTextChatMessage(TextChatMessage msg){
        messageList.add(msg);
    }

    public List<TextChatMessage> exportMessages(){
        return new ArrayList<>(messageList);
    }

}
