package com.chatroom.room;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class Room {

    @Setter
    private String roomId;
    @Setter
    private String name;
    @Setter
    private String prompt;
    @Getter
    private List<User> users;

    public Room(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.users = new ArrayList<>();
        this.prompt = "";
    }

}
