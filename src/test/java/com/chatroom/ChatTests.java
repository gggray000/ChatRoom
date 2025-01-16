package com.chatroom;

import com.chatroom.chat.*;
import com.chatroom.room.Room;
import com.chatroom.room.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChatTests {
    private TextMessageService textMessageService;
    private WebSocketMessageService webSocketMessageService;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
        textMessageService = new TextMessageService(roomService);
        webSocketMessageService = new WebSocketMessageService(roomService);
    }

    @Test
    void testInvalidTextMessages() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId);

        TextMessage msg = new TextMessage(null, testRoomId, "I'm a ghost!");
        textMessageService.saveTextMessage(testRoomId, msg);
        assertEquals(0, textMessageService.messageHistoryMap.get(testRoomId).size());
    }

    @Test
    void testTextMessageStorageAndExport() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId);

        Room testRoom2 = roomService.createRoom("test2");
        String testRoomId2 = testRoom2.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId2);

        TextMessage msg1 = new TextMessage("a", testRoomId, "Hi!");
        TextMessage msg2 = new TextMessage("b", testRoomId, "Hello!!!");
        textMessageService.saveTextMessage(testRoomId, msg1);
        textMessageService.saveTextMessage(testRoomId, msg2);
        assertEquals(2, textMessageService.messageHistoryMap.get(testRoomId).size());

        TextMessage msg3 = new TextMessage("c", testRoomId2, "Hello from another room!");
        textMessageService.saveTextMessage(testRoomId2, msg3);
        assertEquals(2, textMessageService.messageHistoryMap.size());
        assertEquals(2, textMessageService.exportStoredMessages(testRoomId).lines().count());
    }

    @Test
    void testInvalidWebSocketMessage() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        webSocketMessageService.createWebSocketListForRoom(testRoomId);

        WebSocketMessage wsMsg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage wsMsg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .content("Msg without sender")
                .build();

        webSocketMessageService.saveWebSocketMessage(testRoomId, wsMsg1);
        // test storing WebSocketMessage without sender
        webSocketMessageService.saveWebSocketMessage(testRoomId, wsMsg2);
        assertEquals(1, webSocketMessageService.getWebSocketMessageMap().get(testRoomId).size());
        // test storing WebSocketMessage with invalid roomId
        webSocketMessageService.saveWebSocketMessage("999", wsMsg1);
        assertNull(webSocketMessageService.getWebSocketMessageMap().get("999"));
    }

    @Test
    void testWebSocketMessageExport() {

        Room room2 = roomService.createRoom("room2");
        String roomId2 = room2.getRoomId();
        webSocketMessageService.createWebSocketListForRoom(roomId2);

        Room room3 = roomService.createRoom("room3");
        String roomId3 = room3.getRoomId();
        webSocketMessageService.createWebSocketListForRoom(roomId3);

        WebSocketMessage wsMsg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage wsMsg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Ray again")
                .build();

        webSocketMessageService.saveWebSocketMessage(roomId2, wsMsg1);
        webSocketMessageService.saveWebSocketMessage(roomId2, wsMsg2);

        WebSocketMessage wsMsg3 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Ray in another room")
                .build();

        webSocketMessageService.saveWebSocketMessage(roomId3, wsMsg3);

        assertEquals(2, webSocketMessageService.exportWebSocketMessage(roomId2).size());
        assertEquals(1, webSocketMessageService.exportWebSocketMessage(roomId3).size());

    }


}
