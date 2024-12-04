package com.chatroom.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.chatroom.bot.*;

@Controller
public class ChatController {
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    private final TextMessageService textMessageService;
    private final ChatBotController chatBotController;

    @Autowired
    public ChatController(TextMessageService textMessageService, ChatBotController chatBotController) {
        this.textMessageService = textMessageService;
        this.chatBotController = chatBotController;
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage sendMessage(@Payload WebSocketMessage webSocketMessage,
                                        @DestinationVariable String roomId) {

        if(MessageType.CHAT.equals(webSocketMessage.getMessageType())){
            TextMessage textChatMessage = new TextMessage(
                    webSocketMessage.getSender(),
                    webSocketMessage.getContent()
            );
            textMessageService.saveTextMessage(textChatMessage);
        }
        return webSocketMessage;
    }

    @MessageMapping("/chat/{roomId}/relayEndMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage relayEndMessage(@Payload WebSocketMessage endMessage,
                                            @DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        // Get Principal set by WebSocketAuthInterceptor
        Principal user = headerAccessor.getUser();
        // Check if user is admin for this room
        if (user == null || !roomId.equals(user.getName())) {
            throw new MessageDeliveryException("Unauthorized: Only admin can end discussion");
        }
        return endMessage;    }

    @MessageMapping("/chat/{roomId}/endDiscussion")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage generateSummary(@DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor){
        Principal user = headerAccessor.getUser();
        if (user == null || !roomId.equals(user.getName())) {
            throw new MessageDeliveryException("Unauthorized: Only admin can generate summary");
        }
        String summary = chatBotController.makeSummary(textMessageService.exportMessages());
        return WebSocketMessage.builder()
                .messageType(MessageType.SUMMARY)
                .sender("ChatBot - Llama3.2 3B")
                .content(summary)
                .build();
    }

    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage addUser(@Payload WebSocketMessage webSocketMessage,
                                    @DestinationVariable String roomId,
                                    SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", webSocketMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        roomUsers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet());
        roomUsers.get(roomId).add(webSocketMessage.getSender());

        return WebSocketMessage.builder()
                        .messageType(MessageType.USER_LIST)
                        .users(new ArrayList<>(roomUsers.get(roomId)))
                        .build();
    }

    @MessageMapping("/chat/{roomId}/removeUser")
    public void removeUser(@Payload WebSocketMessage chatMessage, String username, String roomId) {
        if (roomId != null && roomUsers.containsKey(roomId)) {
            roomUsers.get(roomId).remove(username);
        }
    }

//    public Set<String> getRoomUsers(String roomId) {
//        return roomUsers.getOrDefault(roomId, Collections.emptySet());
//    }
}