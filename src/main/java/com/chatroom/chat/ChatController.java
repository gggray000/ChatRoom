package com.chatroom.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.chatroom.bot.*;

@Controller
public class ChatController {
    private final Map<String, Set<String>> roomUsers = new ConcurrentHashMap<>();
    private final ChatMessageService chatMessageService;
    private final ChatBotController chatBotController;

    @Autowired
    public ChatController(ChatMessageService chatMessageService, ChatBotController chatBotController) {
        this.chatMessageService = chatMessageService;
        this.chatBotController = chatBotController;
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/public/{roomId}")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage,
                                   @DestinationVariable String roomId) {

        if(MessageType.CHAT.equals(chatMessage.getMessageType())){
            TextChatMessage textChatMessage = new TextChatMessage(
                    chatMessage.getSender(),
                    chatMessage.getContent()
            );
            chatMessageService.saveTextChatMessage(textChatMessage);
        }
        return chatMessage;
    }

    @MessageMapping("/chat/{roomId}/endDiscussion")
    @SendTo("/topic/public/{roomId}")
    public ChatMessage endDiscussion(){
        String summary = chatBotController.chatBot(chatMessageService.exportMessages());
        return ChatMessage.builder()
                .messageType(MessageType.SUMMARY)
                .sender("ChatBot - Llama3.2 3B")
                .content(summary)
                .build();
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

        return ChatMessage.builder()
                        .messageType(MessageType.USER_LIST)
                        .users(new ArrayList<>(roomUsers.get(roomId)))
                        .build();
    }

    @MessageMapping("/chat/{roomId}/removeUser")
    public void removeUser(@Payload ChatMessage chatMessage, String username, String roomId) {
        if (roomId != null && roomUsers.containsKey(roomId)) {
            roomUsers.get(roomId).remove(username);
        }
    }

//    public Set<String> getRoomUsers(String roomId) {
//        return roomUsers.getOrDefault(roomId, Collections.emptySet());
//    }
}