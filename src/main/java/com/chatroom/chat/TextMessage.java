package com.chatroom.chat;

import lombok.Getter;

@Getter
public class TextMessage {
    String userName;
    String roomId;
    String content;

    public TextMessage(String userName, String roomId, String content) {
        this.userName = userName;
        this.roomId = roomId;
        this.content = content;
    }

    @Override
    public String toString(){
        return (this.getUserName() + ": " + this.getContent()+"\n");
    }
}
