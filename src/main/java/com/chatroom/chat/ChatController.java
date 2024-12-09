package com.chatroom.chat;

import com.chatroom.room.JwtService;
import com.chatroom.room.JwtUserDetails;
import com.chatroom.room.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.chatroom.bot.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {
    // Map to store room users with their token IDs
    private final Map<String, Map<String, ChatUser>> roomUsers = new ConcurrentHashMap<>();
    private final TextMessageService textMessageService;
    private final ChatBotController chatBotController;
    private final PdfService pdfService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    // Inner class to store user details
    private static class ChatUser {
        String username;
        String tokenId;
        boolean isAdmin;

        ChatUser(String username, String tokenId, boolean isAdmin) {
            this.username = username;
            this.tokenId = tokenId;
            this.isAdmin = isAdmin;
        }
    }

    @Autowired
    public ChatController(TextMessageService textMessageService,
                          ChatBotController chatBotController,
                          PdfService pdfService,
                          JwtService jwtService, SimpMessagingTemplate simpMessagingTemplate) {
        this.textMessageService = textMessageService;
        this.chatBotController = chatBotController;
        this.pdfService = pdfService;
        this.jwtService = jwtService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    private void validateUserToken(String tokenId, String roomId) {
        JwtUserDetails userDetails = jwtService.validateUserToken(tokenId);
        if (userDetails == null || !userDetails.getRoomId().equals(roomId)) {
            throw new MessageDeliveryException("Invalid or expired token");
        }
    }

    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage addUser(@Payload WebSocketMessage webSocketMessage,
                                    @DestinationVariable String roomId,
                                    SimpMessageHeaderAccessor headerAccessor) {
        String tokenId = webSocketMessage.getTokenId();
        validateUserToken(tokenId, roomId);

        String originalUsername = webSocketMessage.getSender();
        String finalUsername = generateUniqueUsername(originalUsername, roomId);

        // If the username was modified, update the sender in the webSocketMessage
        webSocketMessage.setSender(finalUsername);

        headerAccessor.getSessionAttributes().put("username", webSocketMessage.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.getSessionAttributes().put("tokenId", tokenId);

        roomUsers.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        boolean isAdmin = headerAccessor.getUser() != null &&
                roomId.equals(headerAccessor.getUser().getName());
        ChatUser chatUser = new ChatUser(webSocketMessage.getSender(), tokenId, isAdmin);
        roomUsers.get(roomId).put(tokenId, chatUser);

        simpMessagingTemplate.convertAndSend("/topic/public/" + roomId,
                WebSocketMessage.builder()
                        .sender(finalUsername)
                        .messageType(MessageType.JOIN)
                        .tokenId(tokenId)
                        .build());

        return updateUserList(finalUsername, roomId);
    }

    @MessageMapping("/chat/{roomId}/removeUser")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage removeUser(@Payload WebSocketMessage chatMessage, String username, String roomId) {
        if (roomId != null && roomUsers.containsKey(roomId)) {
            String tokenId = chatMessage.getTokenId(); // Get tokenId from the message
            roomUsers.get(roomId).remove(tokenId);

            return updateUserList(null, roomId);
        }
        return null;
    }

    private WebSocketMessage updateUserList(String finalUsername, String roomId) {
        List<Map<String, Object>> userList = new ArrayList<>();
        roomUsers.get(roomId).values().forEach(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("username", user.username);
            userMap.put("tokenId", user.tokenId);
            userMap.put("isAdmin", user.isAdmin);
            userList.add(userMap);
        });

        return WebSocketMessage.builder()
                .messageType(MessageType.USER_LIST)
                .sender(finalUsername)
                .users(userList)
                .build();
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage sendMessage(@Payload WebSocketMessage webSocketMessage,
                                        @DestinationVariable String roomId) {
        validateUserToken(webSocketMessage.getTokenId(), roomId);

        if(MessageType.CHAT.equals(webSocketMessage.getMessageType())){
            TextMessage textMessage = new TextMessage(
                    webSocketMessage.getSender(),
                    webSocketMessage.getContent()
            );
            textMessageService.saveTextMessage(textMessage);
        }
        return webSocketMessage;
    }

    @MessageMapping("/chat/{roomId}/relayEndMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage relayEndMessage(@Payload WebSocketMessage endMessage,
                                            @DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        if (isAdmin == null || !isAdmin) {
            throw new MessageDeliveryException("Unauthorized: Only admin can generate summary");
        }
        return endMessage;
    }

    @MessageMapping("/chat/{roomId}/endDiscussion")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage generateSummary(@DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        if (isAdmin == null || !isAdmin) {
            throw new MessageDeliveryException("Unauthorized: Only admin can generate summary");
        }

        String summary = chatBotController.makeSummary(textMessageService.exportMessages());
        String pdfFileName = pdfService.makePdf(summary);

        return WebSocketMessage.builder()
                .messageType(MessageType.SUMMARY)
                .sender("ChatBot - Llama3.2 3B")
                .content(summary)
                .resource(pdfFileName)
                .build();
    }

    @GetMapping("/api/pdf/{filename}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable String filename,
            @RequestHeader("Authorization") String token) {

        // Validate the token
        JwtUserDetails userDetails = jwtService.validateUserToken(token.replace("Bearer ", ""));
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Resource resource = pdfService.getUrlResourceOfPdf(filename);
            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String generateUniqueUsername(String baseUsername, String roomId) {
        if (!roomUsers.containsKey(roomId)) {
            return baseUsername;
        }
        boolean usernameTaken = roomUsers.get(roomId).values().stream()
                .anyMatch(user -> user.username.equals(baseUsername));
        if (!usernameTaken) {
            return baseUsername;
        }
        int counter = 2;
        String newUsername;
        do {
            newUsername = baseUsername + "(" + counter + ")";
            final String usernameToCheck = newUsername;
            usernameTaken = roomUsers.get(roomId).values().stream()
                    .anyMatch(user -> user.username.equals(usernameToCheck));
            counter++;
        } while (usernameTaken);
        return newUsername;
    }
}