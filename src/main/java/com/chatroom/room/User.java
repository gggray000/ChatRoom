package com.chatroom.room;

import lombok.Getter;
import lombok.Setter;

@Getter
public class User {

    private final String token;
    @Setter
    private String username;

    public User(String token, String username) {
        this.token = token;
        this.username = username;
    }

}
