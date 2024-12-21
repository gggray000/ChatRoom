package com.chatroom.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

// Temporary in-memory storage for Rooms, in the future need to be changed to use JPA for database

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String name) {
        String roomId = UUID.randomUUID().toString().substring(0, 5);
        while (rooms.containsKey(roomId)) {
            roomId = UUID.randomUUID().toString().substring(0, 5);
        }
        Room room = new Room(roomId, name);
        rooms.put(roomId, room);
        return room;
    }
    // With JPA, this will get room from repository
    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public void deleteRoom(String roomId) {
        if(rooms.get(roomId) != null){
            rooms.remove(roomId);
        }

    }

    public Map<String, Room> getAllRooms() {
        return rooms;
    }

}
