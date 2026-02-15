package com.chatroom.cache;

import com.chatroom.chat.MessageType;
import com.chatroom.chat.WebSocketMessage;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ValkeyWebSocketMessageStore implements MessageStore {

    private final StringRedisTemplate redis;
    private final StreamOperations<String, String, String> streamOps;

    public ValkeyWebSocketMessageStore(StringRedisTemplate redis) {
        this.redis = redis;
        this.streamOps = redis.opsForStream();
    }

    private String key(String roomId) {
        return "stream:room:" + roomId + ":ws";
    }

    public void ensureRoomStreamExists(String roomId, Duration ttl) {
        // Streams are created on first XADD; TTL can be set after that.
        // We’ll set TTL opportunistically in save().
        if (ttl != null) {
            redis.expire(key(roomId), ttl);
        }
    }

    public void append(String roomId, WebSocketMessage msg, Duration ttl, long approxMaxLen) {
        Map<String, String> fields = new HashMap<>();
        fields.put("ts", String.valueOf(msg.getTimestamp()));
        fields.put("sender", msg.getSender());
        fields.put("type", msg.getMessageType().toString());
        fields.put("content", msg.getContent() == null ? "" : msg.getContent());

        MapRecord<String, String, String> record =
                StreamRecords.mapBacked(fields).withStreamKey(key(roomId));

        streamOps.add(record);
        streamOps.trim(key(roomId), approxMaxLen, true);

        if (ttl != null) {
            redis.expire(key(roomId), ttl);
        }
    }


    public List<WebSocketMessage> readAllRetained(String roomId) {
        // XRANGE key - +
        List<MapRecord<String, String, String>> records =
                streamOps.range(key(roomId), Range.unbounded());

        if (records == null || records.isEmpty()) return List.of();

        return records.stream().map(r -> {
            Map<String, String> v = r.getValue();
            WebSocketMessage m = new WebSocketMessage();
            m.setTimestamp(parseLong(v.get("ts")));
            m.setSender(v.get("sender"));
            m.setType(parseType(v.get("type")));
            m.setContent(v.get("content"));
            return m;
        }).toList();
    }

    private static MessageType parseType(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("null")) {
            return MessageType.CHAT; // choose your default
        }
        try {
            return MessageType.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return MessageType.CHAT; // fallback for unexpected values
        }
    }

    public void deleteRoom(String roomId) {
        redis.delete(key(roomId));
    }

    private static long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0L;
        }
    }
}