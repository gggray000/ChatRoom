package com.chatroom.room;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Getter
public class Room {

    @Setter
    private String roomId;
    @Setter
    private String name;
    @Setter
    private String prompt;
    private List<User> users;

    public Room(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.users = new ArrayList<>();
        this.prompt = "";
    }

    public void addUsers(User user){
        this.getUsers().add(user);
    }

    public User findUser(String name){
        for(User user: this.users){
            if (Objects.equals(user.getUsername(), name)){
                return user;
            }
        } return null;
    }

}
