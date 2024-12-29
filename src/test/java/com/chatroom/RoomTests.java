package com.chatroom;

import com.chatroom.room.RoomService;
import com.chatroom.room.Room;
import org.junit.jupiter.api.Test;  // Use JUnit 5 annotation
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;  // Use JUnit 5 assertions
import static org.junit.jupiter.api.Assertions.assertNull;

public class RoomTests {
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
    }

    @Test
    void testCancelRoomCreation() {
        Room room1 = roomService.createRoom("room1");
        String roomId1 = room1.getRoomId();
        Room room2 = roomService.createRoom("room2");
        String roomId2 = room1.getRoomId();
        // test room creation
        assertEquals(2, roomService.getAllRooms().size());
        // test Room deletion
        roomService.deleteRoom(roomId1);
        assertNull(roomService.getRoom(roomId1));
    }
}