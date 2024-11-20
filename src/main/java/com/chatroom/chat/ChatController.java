package com.chatroom.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations messageTemplate;

    @Autowired
    public ChatController(SimpMessageSendingOperations messagingTemplate) {
        this.messageTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/public/{roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   @DestinationVariable String roomId) {
        return chatMessage;
    }

    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/public/{roomId}")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               @DestinationVariable String roomId,
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
        roomUsers.get(roomId).add(chatMessage.getSender());

        messageTemplate.convertAndSend("/topic/public/" + roomId,
                ChatMessage.builder()
                        .messageType(MessageType.USER_LIST)
                        .users(new ArrayList<>(roomUsers.get(roomId)))
                        .build()
        );

        return chatMessage;
    }

    public void removeUser(String username, String roomId) {
        if (roomId != null && roomUsers.containsKey(roomId)) {
            roomUsers.get(roomId).remove(username);
            messageTemplate.convertAndSend("/topic/public/" + roomId,
                    ChatMessage.builder()
                            .messageType(MessageType.LEAVE)
                            .sender(username)
                            .build()
            );
        }
    }

    public Set<String> getRoomUsers(String roomId) {
        return roomUsers.getOrDefault(roomId, Collections.emptySet());
    }
}