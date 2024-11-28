package com.chatroom.room;
import com.chatroom.message.Message;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "room_sequence")
    @SequenceGenerator(name = "room_sequence",
            sequenceName = "room_sequence",
            allocationSize = 1)
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "room")
    @JsonManagedReference
    private List<Message> messages;


    public Room(String name) {
        this.name = name;
    }

    public Room() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}
