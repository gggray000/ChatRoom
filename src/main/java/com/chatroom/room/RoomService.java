package com.chatroom.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

// Now this uses in-storage memory to storage Rooms, in the future need to be changed to use JPA for database

    // Temporary placeholder variable for the repository
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String name) {
        String roomId = UUID.randomUUID().toString();
        Room room = new Room();
        room.setId(Integer.valueOf(roomId));
        room.setName(name);
        rooms.put(roomId, room);
        return room;
    }

    // With JPA, this will get room from repository
    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void deleteRoom(String id) {
        rooms.remove(id);
    }

    public Map<String, Room> getAllRooms() {
        return rooms;
    }
}
