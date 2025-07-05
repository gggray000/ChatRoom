package com.chatroom.room;

import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    @Getter
    @Setter
    List<String> images;
    @Getter
    private PropertyChangeSupport propertyChangeSupport;

    public Room(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
        this.users = new ArrayList<>();
        this.prompt = "";
        this.images = new ArrayList<>();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
}
