package com.chatroom.room;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

// Temporary in-memory storage for Rooms, in the future need to be changed to use JPA for database

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String name) {
        if (name != null) {
            String roomId = UUID.randomUUID().toString().substring(0, 5);
            while (rooms.containsKey(roomId)) {
                roomId = UUID.randomUUID().toString().substring(0, 5);
            }
            Room room = new Room(roomId, name);
            rooms.put(roomId, room);
            room.addPropertyChangeListener(new Cleaner(this, roomId));
            return room;
        }
        return null;
    }
    // With JPA, this will get room from repository
    public Room getRoom(String roomId) {
        if (rooms.containsKey(roomId)) {
            return rooms.get(roomId);
        }
        return null;
    }

    public void addUsers(String roomId, User user) {
        if (!this.getRoom(roomId).getUsers().contains(user)) {
            this.getRoom(roomId).getUsers().add(user);
        }
    }

    public User findUser(String roomId, String name) {
        for (User user : this.getRoom(roomId).getUsers()) {
            if (Objects.equals(user.getUsername(), name)) {
                return user;
            }
        }
        return null;
    }

    public void deleteRoom(String roomId) {
        if(rooms.get(roomId) != null){
            List<String> uploadedImage = rooms.get(roomId).images;
            for (String fileName : uploadedImage) {
                Path imagePath = Paths.get("uploads", fileName);
                try {
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete image: " + fileName + " - " + e.getMessage());
                }
            }
            rooms.remove(roomId);
        }
    }

    public Map<String, Room> getAllRooms() {
        return rooms;
    }

    public String generateUniqueUsername(String baseUsername, String roomId) {
        if (getRoom(roomId) == null) {
            return null;
        }
        if (!isUsernameTaken(baseUsername, roomId)) {
            return baseUsername;
        }
        int counter = 2;
        String uniqueName;
        do {
            uniqueName = baseUsername + "(" + counter + ")";
            counter++;
        } while (isUsernameTaken(uniqueName, roomId));
        return uniqueName;
    }

    private boolean isUsernameTaken(String username, String roomId) {
        return getRoom(roomId).getUsers()
                .stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

}
