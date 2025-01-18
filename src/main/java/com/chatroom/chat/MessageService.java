package com.chatroom.chat;

import java.util.List;

public interface MessageService<T extends Message> {

    void createMessageListForRoom(String roomId);

    void saveMessage(String roomId, T message);

    List<T> exportMessages(String roomId);

    void deleteRoomMessages(String roomId);

}