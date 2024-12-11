package com.chatroom.chat;

import com.chatroom.room.*;
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
import java.util.stream.Collectors;

import com.chatroom.bot.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class ChatController {
    private final TextMessageService textMessageService;
    private final ChatBotController chatBotController;
    private final PdfService pdfService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(TextMessageService textMessageService,
                          ChatBotController chatBotController,
                          PdfService pdfService,
                          JwtService jwtService,
                          SimpMessagingTemplate simpMessagingTemplate,
                          RoomService roomService) {
        this.textMessageService = textMessageService;
        this.chatBotController = chatBotController;
        this.pdfService = pdfService;
        this.jwtService = jwtService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomService = roomService;
    }

    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage addUser(@Payload WebSocketMessage webSocketMessage,
                                    @DestinationVariable String roomId,
                                    SimpMessageHeaderAccessor headerAccessor) {
        String tokenId = webSocketMessage.getTokenId();
        logger.info("Adding user to room {} with token {}", roomId, tokenId);
        JwtUserDetails userDetails = jwtService.validateUserToken(tokenId, roomId);

        String originalUsername = webSocketMessage.getSender();
        String finalUsername = generateUniqueUsername(originalUsername, roomId);

        User user = new User(tokenId, finalUsername);
        roomService.getRoom(roomId).addUsers(user);

        webSocketMessage.setSender(finalUsername);
        logger.info("Current users in room {}: {}", roomId,
                roomService.getRoom(roomId).getUsers().size());

        headerAccessor.getSessionAttributes().put("username", finalUsername);
        headerAccessor.getSessionAttributes().put("roomId", roomId);
        headerAccessor.getSessionAttributes().put("tokenId", tokenId);
        headerAccessor.getSessionAttributes().put("isAdmin", userDetails.isAdmin());

        simpMessagingTemplate.convertAndSend("/topic/public/" + roomId,
                WebSocketMessage.builder()
                        .sender(finalUsername)
                        .messageType(MessageType.JOIN)
                        .tokenId(tokenId)
                        .build());

        return updateUserList(roomId);
    }

    public WebSocketMessage removeUser(String username, String roomId) {
        if (roomId != null && roomService.getRoom(roomId) != null) {
            //String tokenId = webSocketMessage.getTokenId(); // Get tokenId from the message
            roomService.getRoom(roomId)
                         .getUsers()
                         .remove(roomService.getRoom(roomId).findUser(username));
            WebSocketMessage userListMessage =  updateUserList(roomId);
            simpMessagingTemplate.convertAndSend(
                    "/topic/public/" + roomId, userListMessage);
        }
        return null;
    }

    private WebSocketMessage updateUserList(String roomId) {

        List<String> userList = roomService.getRoom(roomId).getUsers()
                .stream()
                .map(User::getUsername)
                .toList();
        System.out.println("Backend List: " + roomService.getRoom(roomId).getUsers());
        System.out.println("Frontend List: " + userList);

        return WebSocketMessage.builder()
                .messageType(MessageType.USER_LIST)
                .userList(userList)
                .build();
    }

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage sendMessage(@Payload WebSocketMessage webSocketMessage,
                                        @DestinationVariable String roomId) {

        if(MessageType.CHAT.equals(webSocketMessage.getMessageType())){
            TextMessage textMessage = new TextMessage(
                    webSocketMessage.getSender(),
                    roomId,
                    webSocketMessage.getContent()
            );
            textMessageService.saveTextMessage(textMessage);
        }
        return webSocketMessage;
    }

    @MessageMapping("/chat/{roomId}/relayEndMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage relayEndMessage(@Payload WebSocketMessage endMessage,
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

        String summary = chatBotController.makeSummary(roomId);
        String pdfFileName = pdfService.makePdf(roomId, summary);

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
            SimpMessageHeaderAccessor headerAccessor) {
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        String token = headerAccessor.getSessionAttributes().get("tokenId").toString();

        JwtUserDetails userDetails = jwtService.validateUserToken(token, roomId);
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
        if (roomService.getRoom(roomId) == null) {
            return baseUsername;
        }
        boolean usernameTaken = roomService.getRoom(roomId).getUsers()
                .stream()
                .anyMatch(user -> user.getUsername().equals(baseUsername));
        if (!usernameTaken) {
            return baseUsername;
        }
        int counter = 2;
        String newUsername;
        do {
            newUsername = baseUsername + "(" + counter + ")";
            final String usernameToCheck = newUsername;
            usernameTaken =roomService.getRoom(roomId).getUsers()
                    .stream()
                    .anyMatch(user -> user.getUsername().equals(usernameToCheck));
            counter++;
        } while (usernameTaken);
        return newUsername;
    }
}