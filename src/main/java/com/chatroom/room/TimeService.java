package com.chatroom.room;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TimeService {
    private Map<String, Integer> globalTimeMap;
    private RoomService roomService;

    public TimeService(RoomService roomService) {
        this.globalTimeMap = new ConcurrentHashMap<>();
        this.roomService = roomService;
    }

    public void setTimeForRoom(String roomId, int timeInSeconds) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            this.globalTimeMap.put(roomId, timeInSeconds);
        }
    }

    public int getTimeForRoom(String roomId) {
        if (this.globalTimeMap.containsKey(roomId)) {
            return this.globalTimeMap.get(roomId);
        }
        System.out.println("Error in getTimeForRoom().");
        ;
        return 0;
    }

    public void deleteRoomTime(String roomId) {
        if (this.globalTimeMap.containsKey(roomId)) {
            globalTimeMap.remove(roomId);
        }
    }


}
