package com.chatroom.room;

import com.chatroom.chat.MessageType;
import com.chatroom.chat.WebSocketMessage;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TimeService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Getter
    private Map<String, Integer> timesOfRooms;
    @Getter
    private Map<String, Thread> timersOfRooms;
    private final RoomService roomService;

    public TimeService(RoomService roomService, SimpMessagingTemplate simpMessagingTemplate) {
        this.timesOfRooms = new ConcurrentHashMap<>();
        this.timersOfRooms = new ConcurrentHashMap<>();
        this.roomService = roomService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void setUpRoomTimer(String roomId, int timeInSeconds) {
        if (timersOfRooms.containsKey(roomId)) {
            System.out.println("Timer already running for Room " + roomId);
            return;
        }

        Thread timerThread = Thread.startVirtualThread(() -> {
            try {
                while (getTimeForRoom(roomId) > 0) {
                    Thread.sleep(1000); // Wait 1 second
                    int newTime = getTimeForRoom(roomId) - 1;
                    setTimeForRoom(roomId, newTime);

                    WebSocketMessage message = WebSocketMessage.builder()
                            .messageType(MessageType.UPDATE_TIME)
                            .timeInSeconds(newTime)
                            .build();
                    simpMessagingTemplate.convertAndSend("/topic/public/" + roomId, message);
                }
                stopRoomTimer(roomId, true);
            } catch (InterruptedException e) {
                System.out.println("Timer for room " + roomId + " interrupted.");
            }
        });
        timersOfRooms.put(roomId, timerThread);

        WebSocketMessage message = WebSocketMessage.builder()
                .messageType(MessageType.TIMER_START)
                .build();
        simpMessagingTemplate.convertAndSend("/topic/public/" + roomId, message);
    }

    public void stopRoomTimer(String roomId, boolean ifTimesUp) {
        Thread timerThread = timersOfRooms.get(roomId);
        if (timerThread != null) {
            timerThread.interrupt();
            timersOfRooms.remove(roomId);
        } else {
            System.out.println("No active timer found for room: " + roomId);
            return;
        }

        WebSocketMessage message = WebSocketMessage.builder()
                .messageType(ifTimesUp ? MessageType.TIMES_UP : MessageType.TIMER_PAUSE)
                .build();
        simpMessagingTemplate.convertAndSend("/topic/public/" + roomId, message);
    }

    public void setTimeForRoom(String roomId, int timeInSeconds) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            this.timesOfRooms.put(roomId, timeInSeconds);
        }
    }

    public int getTimeForRoom(String roomId) {
        if (this.timesOfRooms.containsKey(roomId)) {
            return this.timesOfRooms.get(roomId);
        }
        System.out.println("Error in getTimeForRoom().");
        return 0;
    }

    public void deleteRoomTime(String roomId) {
        if (this.timesOfRooms.containsKey(roomId)) {
            timesOfRooms.remove(roomId);
        }
        if (this.timersOfRooms.containsKey(roomId)) {
            timersOfRooms.remove(roomId);
        }
    }
}
