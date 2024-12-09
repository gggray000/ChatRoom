package com.chatroom.chat;

public class TextMessage {
    String userName;
    String roomId;
    String content;

    public TextMessage(String userName, String roomId, String content) {
        this.userName = userName;
        this.roomId = roomId;
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public String getRoomId() {
        return roomId;
    }

    @Override
    public String toString(){
        return (this.getUserName() + ": " + this.getContent()+"\n");
    }
}
