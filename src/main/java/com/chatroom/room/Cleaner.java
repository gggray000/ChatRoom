package com.chatroom.room;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static java.lang.Thread.sleep;

public class Cleaner implements PropertyChangeListener {
    private final RoomService roomService;
    private final String roomId;

    public Cleaner(RoomService roomService, String roomId) {
        this.roomService = roomService;
        this.roomId = roomId;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        int userCount = (int) event.getNewValue();
        if (userCount == 0) {
            try {
                System.out.println("Last user left, possible clean-up signal.");
                sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (roomService.getRoom(roomId).getUsers().isEmpty()) {
                System.out.println("After 5s delay, executing clean-up procedure...");
                roomService.deleteRoom(roomId);
            }
        }
    }
}
