package com.chatroom.chat;

import com.chatroom.cache.MessageStore;
import com.chatroom.room.RoomService;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebSocketMessageService implements MessageService<WebSocketMessage> {

    @Getter
    private Map<String, List<WebSocketMessage>> messageHistoryMap = new ConcurrentHashMap<>();
    private final RoomService roomService;
    private final MessageStore store;
    private static final long STREAM_MAXLEN_APPROX = 2000;
    private static final Duration ROOM_TTL = Duration.ofHours(24);

    @Autowired
    public WebSocketMessageService(RoomService roomService, MessageStore store) {
        this.roomService = roomService;
        this.store = store;
    }

    @Override
    public void createMessageListForRoom(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId) && !messageHistoryMap.containsKey(roomId)) {
            //messageHistoryMap.put(roomId, new ArrayList<>());
            store.ensureRoomStreamExists(roomId, ROOM_TTL);
        }
    }

    @Override
    public void saveMessage(String roomId, WebSocketMessage message) {
        if (roomService.getAllRooms().containsKey(roomId) && message.getSender() != null) {
            //messageHistoryMap.get(roomId).add(message);
            store.append(roomId, message, ROOM_TTL, STREAM_MAXLEN_APPROX);
        }
    }

    @Override
    public List<WebSocketMessage> exportMessages(String roomId) {
        // Return the list of messages for this room, or empty list if none exist
        // List<WebSocketMessage> history = messageHistoryMap.getOrDefault(roomId, Collections.emptyList());
        // history.sort(Comparator.comparingLong(WebSocketMessage::getTimestamp));
        // return history;
        return store.readAllRetained(roomId);
    }

    public String messageHistoryToString(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            return exportMessages(roomId).stream()
                    .map(
                            webSocketMessage ->
                                    webSocketMessage.getSender() + ": "
                                            + Jsoup.parse(webSocketMessage.getContent()).text() + "\n"
                    )
                    .collect(Collectors.joining());
        }
        return "";
    }


    @Override
    public void deleteRoomMessages(String roomId) {
        if (roomService.getAllRooms().containsKey(roomId)) {
            //messageHistoryMap.remove(roomId);
            store.deleteRoom(roomId);
        }
    }

}
