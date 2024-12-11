package com.chatroom.room;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Room {

    private String roomId;
    private String name;
    private List<User> users;

    public Room(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.users = new ArrayList<>();
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers(){
        return this.users;
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
