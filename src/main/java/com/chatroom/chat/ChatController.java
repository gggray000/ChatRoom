package com.chatroom.chat;

import com.chatroom.bot.ChatBotController;
import com.chatroom.room.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Controller
public class ChatController {
    @Value("${langchain4j.ollama.chat-model.model-name}")
    private String modelName;
    private final TextMessageService textMessageService;
    private final WebSocketMessageService webSocketMessageService;
    private final ChatBotController chatBotController;
    private final PdfService pdfService;
    private final JwtService jwtService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomService roomService;
    private final TimeService timeService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(TextMessageService textMessageService, WebSocketMessageService webSocketMessageService,
                          ChatBotController chatBotController,
                          PdfService pdfService,
                          JwtService jwtService,
                          SimpMessagingTemplate simpMessagingTemplate,
                          RoomService roomService, TimeService timeService) {
        this.textMessageService = textMessageService;
        this.webSocketMessageService = webSocketMessageService;
        this.chatBotController = chatBotController;
        this.pdfService = pdfService;
        this.jwtService = jwtService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.roomService = roomService;
        this.timeService = timeService;
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
        String finalUsername = roomService.generateUniqueUsername(originalUsername, roomId);

        if (userDetails != null && finalUsername != null) {
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
        return null;
    }

    public void removeUser(String username, String roomId) {
        if (roomId != null && roomService.getRoom(roomId) != null) {
            roomService.getRoom(roomId)
                         .getUsers()
                         .remove(roomService.getRoom(roomId).findUser(username));
            WebSocketMessage userListMessage =  updateUserList(roomId);
            simpMessagingTemplate.convertAndSend(
                    "/topic/public/" + roomId, userListMessage);
        }
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
            webSocketMessageService.saveWebSocketMessage(roomId, webSocketMessage);

            TextMessage textMessage = new TextMessage(
                    webSocketMessage.getSender(),
                    roomId,
                    webSocketMessage.getContent()
            );
            textMessageService.saveTextMessage(textMessage);
        }
        return webSocketMessage;
    }

    @MessageMapping("/chat/{roomId}/history")
    public void displayHistory(@DestinationVariable String roomId,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getSessionAttributes().get("username").toString();
        List<WebSocketMessage> history = webSocketMessageService.exportWebSocketMessage(roomId);
        if (!history.isEmpty()) {
            for (WebSocketMessage message : history) {
                simpMessagingTemplate.convertAndSend(
                        "/topic/private/" + roomId + "/" + username, message
                );
            }
            WebSocketMessage notice = WebSocketMessage.builder()
                    .messageType(MessageType.SHOW_HISTORY)
                    .build();
            simpMessagingTemplate.convertAndSend(
                    "/topic/private/" + roomId + "/" + username, notice
            );
        }
    }

    @MessageMapping("/chat/{roomId}/relayGetSummaryMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage relaySummarizeMessage(@Payload WebSocketMessage getSummaryMessage,
                                            @DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(getSummaryMessage.getTokenId(), roomId);
        if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can generate summary");
        }
        return getSummaryMessage;
    }

    @MessageMapping("/chat/{roomId}/summarize")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage generateSummary(@Payload WebSocketMessage getSummaryMessage,
                                            @DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(getSummaryMessage.getTokenId(), roomId);
        if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can generate summary");
        }

        String summary = chatBotController.makeSummary(roomId);
        String pdfFileName = pdfService.makePdf(roomId, summary);
        WebSocketMessage summaryMessage =
                WebSocketMessage.builder()
                        .messageType(MessageType.SUMMARY)
                        .sender("ChatBot - " + modelName)
                        .content(summary)
                        .resource(pdfFileName)
                        .build();
        webSocketMessageService.saveWebSocketMessage(roomId, summaryMessage);
        return summaryMessage;
    }

    @GetMapping("/api/pdf/{filename}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable String filename,
            @RequestHeader(value = "roomId") String roomId,
            @RequestHeader(value = "token") String token) {
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

    @MessageMapping("/chat/{roomId}/updateTime")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage updateTime(@Payload WebSocketMessage updateTimeMessage,
                                       @DestinationVariable String roomId,
                                       SimpMessageHeaderAccessor headerAccessor) {
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(updateTimeMessage.getTokenId(), roomId);
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        Integer newTime = updateTimeMessage.getTimeInSeconds();
        if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can update time.");
        } else if (newTime > timeService.getTimeForRoom(roomId)) {
            throw new MessageDeliveryException("Invalid time update.");
        } else {
            timeService.setTimeForRoom(roomId, newTime);
        }
        return updateTimeMessage;
    }

    @MessageMapping("/chat/{roomId}/timerOperation")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage operateTimer(@Payload WebSocketMessage operateTimerMessage,
                                         @DestinationVariable String roomId,
                                         SimpMessageHeaderAccessor headerAccessor) {
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(operateTimerMessage.getTokenId(), roomId);
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can operate timer.");
        }
        return operateTimerMessage;
    }

}