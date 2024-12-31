package com.chatroom;

import com.chatroom.room.RoomService;
import com.chatroom.room.Room;
import org.junit.jupiter.api.Test;  // Use JUnit 5 annotation
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(1, roomService.getAllRooms().size());
    }

    @Test
    void testDuplicatedRoom(){
        Room room3 = roomService.createRoom("test");
        Room room4 = roomService.createRoom("test");
        assertFalse(room3.getRoomId()==room4.getRoomId());
        assertEquals(2, roomService.getAllRooms().size());

    }
}