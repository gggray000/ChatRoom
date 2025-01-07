package com.chatroom;

import com.chatroom.chat.*;
import com.chatroom.room.Room;
import com.chatroom.room.RoomService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChatTests {
    private TextMessageService textMessageService;
    private WebSocketMessageService webSocketMessageService;
    private RoomService roomService;

    @Test
    void testInvalidTextMessages() {
        textMessageService = new TextMessageService();

        TextMessage msg3 = new TextMessage(null, "003", "I'm a ghost!");
        textMessageService.saveTextMessage(msg3);
        assertEquals(0, textMessageService.messageList.size());
    }

    @Test
    void testTextMessageExport() {
        textMessageService = new TextMessageService();

        TextMessage msg1 = new TextMessage("a", "001", "Hi!");
        TextMessage msg2 = new TextMessage("b", "001", "Hello!!!");
        textMessageService.saveTextMessage(msg1);
        textMessageService.saveTextMessage(msg2);
        assertEquals(2, textMessageService.messageList.size());

        TextMessage msg3 = new TextMessage("c", "002", "Hello from another room!");
        System.out.println(textMessageService.exportStoredMessages("001"));
        assertEquals(2, textMessageService.exportStoredMessages("001").lines().count());
    }

    @Test
    void testInvalidWebSocketMessage() {
        roomService = new RoomService();
        webSocketMessageService = new WebSocketMessageService(roomService);

        Room room1 = roomService.createRoom("room1");
        String roomId1 = room1.getRoomId();

        WebSocketMessage msg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage msg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .content("Msg without sender")
                .build();

        webSocketMessageService.saveWebSocketMessage(roomId1, msg1);
        // test storing WebSocketMessage without sender
        webSocketMessageService.saveWebSocketMessage(roomId1, msg2);
        assertEquals(1, webSocketMessageService.getWebSocketMessageMap().get(roomId1).size());
        // test storing WebSocketMessage with invalid roomId
        webSocketMessageService.saveWebSocketMessage("999", msg1);
        assertNull(webSocketMessageService.getWebSocketMessageMap().get("999"));
    }

    @Test
    void testWebSocketMessageExport() {
        roomService = new RoomService();
        webSocketMessageService = new WebSocketMessageService(roomService);

        Room room2 = roomService.createRoom("room2");
        String roomId2 = room2.getRoomId();

        Room room3 = roomService.createRoom("room3");
        String roomId1 = room3.getRoomId();

        WebSocketMessage msg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage msg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg without sender")
                .build();

        webSocketMessageService.saveWebSocketMessage(roomId1, msg1);
        webSocketMessageService.saveWebSocketMessage(roomId1, msg2);

        WebSocketMessage msg3 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        webSocketMessageService.saveWebSocketMessage(roomId2, msg3);

        assertEquals(2, webSocketMessageService.exportWebSocketMessage(roomId1).size());
        assertEquals(1, webSocketMessageService.exportWebSocketMessage(roomId2).size());


    }


}
