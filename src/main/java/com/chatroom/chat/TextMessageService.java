package com.chatroom.chat;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextMessageService {

    public List<TextMessage> messageList = new ArrayList<>();

    public void saveTextMessage(TextMessage msg){
        messageList.add(msg);
    }

    public List<TextMessage> exportMessages(){
        return new ArrayList<>(messageList);
    }

}
