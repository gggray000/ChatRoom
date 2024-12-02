package com.chatroom.chat;

public class TextMessage {
    String userName;
    String content;

    public TextMessage(String userName, String content) {
        this.userName = userName;
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString(){
        return (this.getUserName() + ": " + this.getContent()+"\n");
    }
}
