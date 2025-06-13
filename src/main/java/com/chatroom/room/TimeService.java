package com.chatroom.room;

import com.chatroom.chat.MessageType;
import com.chatroom.chat.WebSocketMessage;
import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
public class TimeService {
    private final SimpMessagingTemplate simpMessagingTemplate;
    @Getter
    private Map<String, Integer> timesOfRooms;
    @Getter
    private Map<String, ScheduledFuture<?>> timersOfRooms;
    private final RoomService roomService;
    private final int threadsLimit = Runtime.getRuntime().availableProcessors();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadsLimit);

    public TimeService(RoomService roomService, SimpMessagingTemplate simpMessagingTemplate) {
        this.timesOfRooms = new ConcurrentHashMap<>();
        this.timersOfRooms = new ConcurrentHashMap<>();
        this.roomService = roomService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        System.out.println("Thread Limits: " + threadsLimit);
    }

    public void setUpRoomTimer(String roomId, int timeInSeconds) {
        if (timersOfRooms.containsKey(roomId)) {
            System.out.println("Timer already running for Room " + roomId);
            return;
        }

        final Runnable timer = new Runnable() {
            @Override
            public void run() {
                int remainingTime = getTimeForRoom(roomId);
                if (remainingTime == 0) {
                    stopRoomTimer(roomId, true);
                    return;
                }
                int newTime = remainingTime - 1;
                setTimeForRoom(roomId, newTime);

                WebSocketMessage message = WebSocketMessage.builder()
                        .messageType(MessageType.UPDATE_TIME)
                        .timeInSeconds(newTime)
                        .build();
                simpMessagingTemplate.convertAndSend("/topic/public/" + roomId, message);
            }
        };
        setTimeForRoom(roomId, timeInSeconds);

        final ScheduledFuture<?> timerHandler =
                scheduler.scheduleAtFixedRate(timer, 0, 1, TimeUnit.SECONDS);
        timersOfRooms.put(roomId, timerHandler);

        WebSocketMessage message = WebSocketMessage.builder()
                .messageType(MessageType.TIMER_START)
                .build();
        simpMessagingTemplate.convertAndSend("/topic/public/" + roomId, message);
    }

    public void stopRoomTimer(String roomId, boolean ifTimesUp) {
        ScheduledFuture<?> future = timersOfRooms.get(roomId);
        if (future != null) {
            future.cancel(true);
            timersOfRooms.remove(roomId);
            deleteRoomTime(roomId);
        } else {
            System.out.println("No active timer found for room: " + roomId);
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
