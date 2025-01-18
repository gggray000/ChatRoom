package com.chatroom.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage extends Message {

    private MessageType messageType;
    private List<String> userList;
    private String tokenId;
    private String resource;
    private int timeInSeconds;

    public void setSender(String username) {
        this.sender = username;
    }

}
