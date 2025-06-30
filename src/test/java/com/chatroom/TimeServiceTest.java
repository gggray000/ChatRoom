package com.chatroom;

import com.chatroom.chat.MessageType;
import com.chatroom.chat.WebSocketMessage;
import com.chatroom.room.Room;
import com.chatroom.room.RoomService;
import com.chatroom.room.TimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimeServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TimeService timeService;

    private final String roomId = "testRoom";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(roomService.getAllRooms()).thenReturn(new HashMap<>(Map.of(roomId, new Room(roomId, "Test Room"))));
    }

    @Test
    void testSetUpRoomTimer() {
        timeService.setUpRoomTimer(roomId, 60);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public/" + roomId), messageCaptor.capture());

        Object sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage instanceof WebSocketMessage);

        WebSocketMessage msg = (WebSocketMessage) sentMessage;
        assertEquals(MessageType.TIMER_START, msg.getMessageType());
    }

    @Test
    void testMultipleTimer() {
        timeService.getTimersOfRooms().put(roomId, mock(Thread.class));
        timeService.setUpRoomTimer(roomId, 60);
        verify(messagingTemplate, never()).convertAndSend((String) eq("/topic/public/" + roomId), (Object) any());
    }

    @Test
    void testStopRoomTimer() {
        Thread mockTimerThread = mock(Thread.class);
        timeService.setTimeForRoom(roomId, 300);
        timeService.getTimersOfRooms().put(roomId, mockTimerThread);
        timeService.stopRoomTimer(roomId, true);
        verify(mockTimerThread).interrupt();
        assertEquals(300, timeService.getTimeForRoom(roomId)); // Test if the time is still persisted after thread being interrupted.

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public/" + roomId), messageCaptor.capture());

        Object capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage instanceof WebSocketMessage);

        WebSocketMessage wsMessage = (WebSocketMessage) capturedMessage;
        assertEquals(MessageType.TIMES_UP, wsMessage.getMessageType());

        assertFalse(timeService.getTimersOfRooms().containsKey(roomId));
    }
}
