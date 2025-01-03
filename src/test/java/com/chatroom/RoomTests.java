package com.chatroom;

import com.chatroom.room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Test
    void testJwtService() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "chat-room-validate");
        String token1 = jwtService.generateUserToken("Admin", "001", true);
        System.out.println(token1);
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(token1, "001");
        assertTrue(jwtUserDetails.isAdmin());
        // test token forgery
        String token2 = token1 + "admin";
        assertNull(jwtService.validateUserToken(token2, "001"));
        // test wrong room id
        assertNull(jwtService.validateUserToken(token1, "0001"));
    }

    @Test
    void testUrlCreation() {
        UrlService urlService = new UrlService(roomService);
        Room room5 = roomService.createRoom("test");
        String roomId5 = room5.getRoomId();
        String url = urlService.createUrl(roomId5);
        assertEquals(1, urlService.getRoomUrlTable().size());
        // simulate URL generation for QrCodeService, which calls the same function another time
        assertEquals(url, urlService.createUrl(roomId5));
        // test wrong roomId
        assertNull(urlService.createUrl("abcd1"));
    }
}