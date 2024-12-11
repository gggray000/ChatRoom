package com.chatroom.room;

public class User {

    private final String token;
    private String username;

    public User(String token, String username) {
        this.token = token;
        this.username = username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }
}
