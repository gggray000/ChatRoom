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
import java.util.concurrent.ScheduledFuture;

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
        timeService.getTimers().put(roomId, mock(ScheduledFuture.class));
        timeService.setUpRoomTimer(roomId, 60);
        verify(messagingTemplate, never()).convertAndSend((String) eq("/topic/public/" + roomId), (Object) any());
    }

    @Test
    void testStopRoomTimer() {
        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        timeService.getTimers().put(roomId, mockFuture);
        timeService.stopRoomTimer(roomId, true);
        verify(mockFuture).cancel(true);

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/public/" + roomId), messageCaptor.capture());

        Object capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage instanceof WebSocketMessage);

        WebSocketMessage wsMessage = (WebSocketMessage) capturedMessage;
        assertEquals(MessageType.TIMES_UP, wsMessage.getMessageType());

        assertFalse(timeService.getTimers().containsKey(roomId));
    }
}
