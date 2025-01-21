package com.chatroom.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@AllArgsConstructor
@SuperBuilder
public class TextMessage extends Message {

    public TextMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
    }

    @Override
    public String toString(){
        return (this.getSender() + ": " + this.getContent() + "\n");
    }
}
