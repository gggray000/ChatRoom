package com.chatroom;

import com.chatroom.chat.*;
import com.chatroom.room.Room;
import com.chatroom.room.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ChatTests {
    private TextMessageService textMessageService;
    private WebSocketMessageService webSocketMessageService;
    private RoomService roomService;
    private MockValkey mockValkey;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
        textMessageService = new TextMessageService(roomService);
        webSocketMessageService = new WebSocketMessageService(roomService, mockValkey);
    }

    @Test
    void testInvalidTextMessages() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId);

        TextMessage msg = new TextMessage(null, "I'm a ghost!");
        textMessageService.saveMessage(testRoomId, msg);
        assertEquals(0, textMessageService.getMessageHistoryMap().get(testRoomId).size());
    }

    @Test
    void testTextMessageStorageAndExport() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId);

        Room testRoom2 = roomService.createRoom("test2");
        String testRoomId2 = testRoom2.getRoomId();
        textMessageService.createMessageListForRoom(testRoomId2);

        TextMessage msg1 = new TextMessage("a", "Hi!");
        TextMessage msg2 = new TextMessage("b", "Hello!!!");
        TextMessage msg3 = new TextMessage("c", "Hello but later!");
        msg1.setTimestamp(System.currentTimeMillis());
        msg2.setTimestamp(System.currentTimeMillis());
        msg3.setTimestamp(System.currentTimeMillis());
        textMessageService.saveMessage(testRoomId, msg1);
        textMessageService.saveMessage(testRoomId, msg2);
        textMessageService.saveMessage(testRoomId, msg3);
        assertEquals(3, textMessageService.getMessageHistoryMap().get(testRoomId).size());

        TextMessage msg4 = new TextMessage("d", "Hello from another room!");
        assertEquals(2, textMessageService.getMessageHistoryMap().size());

        List<TextMessage> exportedMessages = textMessageService.exportMessages(testRoomId);
        assertEquals("Hi!", exportedMessages.get(0).getContent());
        assertEquals("Hello!!!", exportedMessages.get(1).getContent());
        assertEquals("Hello but later!", exportedMessages.get(2).getContent());
        assertEquals(3, textMessageService.messageHistoryToString(testRoomId).lines().count());
        
    }

    @Test
    void testInvalidWebSocketMessage() {

        Room testRoom = roomService.createRoom("test");
        String testRoomId = testRoom.getRoomId();
        webSocketMessageService.createMessageListForRoom(testRoomId);

        WebSocketMessage wsMsg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .build();

        WebSocketMessage wsMsg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .content("Msg without sender")
                .build();

        webSocketMessageService.saveMessage(testRoomId, wsMsg1);
        // test storing WebSocketMessage without sender
        webSocketMessageService.saveMessage(testRoomId, wsMsg2);
        assertEquals(1, webSocketMessageService.getMessageHistoryMap().get(testRoomId).size());
        // test storing WebSocketMessage with invalid roomId
        webSocketMessageService.saveMessage("999", wsMsg1);
        assertNull(webSocketMessageService.getMessageHistoryMap().get("999"));
    }

    @Test
    void testWebSocketMessageExport() {

        Room room2 = roomService.createRoom("room2");
        String roomId2 = room2.getRoomId();
        webSocketMessageService.createMessageListForRoom(roomId2);

        Room room3 = roomService.createRoom("room3");
        String roomId3 = room3.getRoomId();
        webSocketMessageService.createMessageListForRoom(roomId3);

        WebSocketMessage wsMsg1 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Msg from Ray")
                .timestamp(System.currentTimeMillis())
                .build();

        WebSocketMessage wsMsg2 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Ray again")
                .timestamp(System.currentTimeMillis())
                .build();

        webSocketMessageService.saveMessage(roomId2, wsMsg1);
        webSocketMessageService.saveMessage(roomId2, wsMsg2);

        WebSocketMessage wsMsg3 = WebSocketMessage.builder()
                .messageType(MessageType.CHAT)
                .sender("Ray")
                .content("Ray in another room")
                .build();

        webSocketMessageService.saveMessage(roomId3, wsMsg3);

        assertEquals(2, webSocketMessageService.exportMessages(roomId2).size());
        assertEquals(1, webSocketMessageService.exportMessages(roomId3).size());

        WebSocketMessage wsMsg4 = WebSocketMessage.builder()
                .messageType(MessageType.SUMMARY)
                .sender("Bot")
                .content("A Summary")
                .timestamp(System.currentTimeMillis())
                .build();

        webSocketMessageService.saveMessage(roomId2, wsMsg4);
        assertEquals(3, webSocketMessageService.exportMessages(roomId2).size());

        List<WebSocketMessage> exportedMessagesRoom2 = webSocketMessageService.exportMessages(roomId2);
        assertEquals("Msg from Ray", exportedMessagesRoom2.get(0).getContent());
        assertEquals("Ray again", exportedMessagesRoom2.get(1).getContent());
        assertEquals("A Summary", exportedMessagesRoom2.get(2).getContent());

    }


}
