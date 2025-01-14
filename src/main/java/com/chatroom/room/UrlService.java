package com.chatroom.room;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UrlService {

    private final RoomService roomService;
    @Getter
    private Map<Integer, String> roomUrlTable = new ConcurrentHashMap<>();

    @Autowired
    public UrlService(RoomService roomService) {
        this.roomService = roomService;
    }

    public String createUrl(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            return "/chat/" + generateUrlNumber(roomId);
        }
        return null;
    }

    public Integer generateUrlNumber(String roomId) {
        // First find existing urlNumber
        for (Map.Entry<Integer, String> entry : roomUrlTable.entrySet()) {
            if (Objects.equals(roomId, entry.getValue())) {
                return entry.getKey();
            }
        }
        Integer urlNumber = (int) (Math.random() * 1000) + 1;
        roomUrlTable.put(urlNumber, roomId);
        return urlNumber;
    }

    public String getRoomIdFromTable(Integer urlNumber) {
        if (this.roomUrlTable.containsKey(urlNumber)) {
            return roomUrlTable.get(urlNumber);
        } else {
            return null;
        }
    }
}
