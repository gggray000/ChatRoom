package com.chatroom.room;

// Now this is idled, need to be used for JPA in the future
public class RoomRequest {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
