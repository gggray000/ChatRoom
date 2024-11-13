package com.chatroom.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private Set<String> connectedUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final SimpMessageSendingOperations messageTemplate;

    //injecting SimpMessageSendingOperations allows server to send messages to clients.
    @Autowired
    public ChatController(SimpMessageSendingOperations messagingTemplate) {
        this.messageTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage){
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor){
        //Add username in websocket session
        headerAccessor.getSessionAttributes()
                      .put("username", chatMessage.getSender());
        connectedUsers.add(chatMessage.getSender());
        // Send current user list to all clients
        messageTemplate.convertAndSend("/topic/public",
                ChatMessage.builder()
                        .messageType(MessageType.USER_LIST)
                        .users(new ArrayList<>(connectedUsers))
                        .build()
                );
        return chatMessage;
    }

    public Set<String> getConnectedUsers() {
        return connectedUsers;
    }

    public void removeUser(String username) {
        connectedUsers.remove(username);
    }

}
