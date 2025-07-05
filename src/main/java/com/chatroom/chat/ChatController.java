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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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

    @PostMapping("/chat/{roomId}/verifyUsername")
    @ResponseBody
    public ResponseEntity<String> verifyUsername(@RequestBody Map<String, String> request,
                                                 @PathVariable String roomId) {
        try {
            String originalUsername = request.get("username");
            System.out.println("originalUsername: " + originalUsername);
            System.out.println("roomId: " + roomId);
            String finalUsername = roomService.generateUniqueUsername(originalUsername, roomId);
            System.out.println("finalUsername: " + finalUsername);
            return ResponseEntity.ok(finalUsername);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error verifying username: " + e.getMessage());
        }
    }

    @MessageMapping("/chat/{roomId}/addUser")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage addUser(@Payload WebSocketMessage webSocketMessage,
                                    @DestinationVariable String roomId,
                                    SimpMessageHeaderAccessor headerAccessor) {

        String tokenId = webSocketMessage.getTokenId();
        logger.info("Adding user {} to room {} with token {}", webSocketMessage.getSender(), roomId, tokenId);

        JwtUserDetails userDetails = jwtService.validateUserToken(tokenId, roomId);

        if (userDetails != null) {
            User user = new User(tokenId, webSocketMessage.getSender());
            roomService.addUsers(roomId, user);

            logger.info("Current users in room {}: {}", roomId,
                    roomService.getRoom(roomId).getUsers().size());

            headerAccessor.getSessionAttributes().put("username", webSocketMessage.getSender());
            System.out.println("Right now header is: " + headerAccessor.getSessionAttributes().get("username").toString());
            headerAccessor.getSessionAttributes().put("roomId", roomId);
            headerAccessor.getSessionAttributes().put("tokenId", tokenId);
            headerAccessor.getSessionAttributes().put("isAdmin", userDetails.isAdmin());

            simpMessagingTemplate.convertAndSend("/topic/public/" + roomId,
                    WebSocketMessage.builder()
                            .sender(webSocketMessage.getSender())
                            .messageType(MessageType.JOIN)
                            .tokenId(tokenId)
                            .build());

            return updateUserList(roomId);
        }
        return null;
    }

    public void removeUser(String username, String roomId) {
        if (roomId != null && roomService.getRoom(roomId) != null) {
            Room room = roomService.getRoom(roomId);
            int oldUserCount = room.getUsers().size();
            room.getUsers()
                    .remove(roomService.findUser(roomId, username));
            room.getPropertyChangeSupport()
                    .firePropertyChange(
                            "userCount changes",
                            oldUserCount,
                            room.getUsers().size()
                    );
            if (oldUserCount > 1) {
                WebSocketMessage userListMessage = updateUserList(roomId);
                simpMessagingTemplate.convertAndSend(
                        "/topic/public/" + roomId, userListMessage);
            }
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
            long timeStamp = System.currentTimeMillis();
            webSocketMessage.setTimestamp(timeStamp);
            webSocketMessageService.saveMessage(roomId, webSocketMessage);

            TextMessage textMessage = TextMessage.builder()
                    .sender(webSocketMessage.getSender())
                    .content(webSocketMessage.getContent())
                    .timestamp(timeStamp)
                    .build();

            textMessage.setTimestamp(timeStamp);
            textMessageService.saveMessage(roomId, textMessage);
        }
        return webSocketMessage;
    }

    @PostMapping("/chat/{roomId}/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@PathVariable("roomId") String roomId, @RequestBody MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }

        try {
            Room room = roomService.getRoom(roomId);
            if (room == null) throw new Exception("Room does not exists");
            // Generate safe file name
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf('.'));
            String fileName = UUID.randomUUID() + extension;
            room.getImages().add(fileName);

            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // Return public URL
            String imageUrl = "/uploads/" + fileName;
            return ResponseEntity.ok(Map.of("url", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image"));
        }
    }

    @MessageMapping("/chat/{roomId}/history")
    public void displayHistory(@Payload WebSocketMessage webSocketMessage,
                               @DestinationVariable String roomId,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = webSocketMessage.getSender();
        logger.info("User: {} trying to get history.", username);
        logger.info("History sent to: {}", "/topic/private/" + roomId + "/" + username);
        List<WebSocketMessage> history = webSocketMessageService.exportMessages(roomId);
        if (!history.isEmpty()) {
            simpMessagingTemplate.convertAndSend(
                    "/topic/private/" + roomId + "/" + username,
                    WebSocketMessage.builder()
                            .messageType(MessageType.SHOW_HISTORY)
                            .history(history)
                            .build()
            );
        }
    }

    @MessageMapping("/chat/{roomId}/relayGetSummaryMessage")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage relaySummarizeMessage(@Payload WebSocketMessage getSummaryMessage,
                                            @DestinationVariable String roomId,
                                            SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        //JwtUserDetails jwtUserDetails = jwtService.validateUserToken(getSummaryMessage.getTokenId(), roomId);
        if (isAdmin == null || !isAdmin) {
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
        //JwtUserDetails jwtUserDetails = jwtService.validateUserToken(getSummaryMessage.getTokenId(), roomId);
        if (isAdmin == null || !isAdmin) {
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
                        .timestamp(System.currentTimeMillis())
                        .build();
        webSocketMessageService.saveMessage(roomId, summaryMessage);
        return summaryMessage;
    }

    @GetMapping("/api/pdf/{filename}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable String filename,
            @RequestHeader(value = "roomId") String roomId,
            @RequestHeader(value = "token") String token) {
        JwtUserDetails userDetails = jwtService.validateUserToken(token, roomId);
        if (userDetails == null || !Objects.equals(userDetails.getRoomId(), roomId)) {
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

    @MessageMapping("/chat/{roomId}/setTimer")
    public void setUpTimer(@Payload WebSocketMessage updateTimeMessage,
                           @DestinationVariable("roomId") String roomId,
                           SimpMessageHeaderAccessor headerAccessor) {
        //JwtUserDetails jwtUserDetails = jwtService.validateUserToken(updateTimeMessage.getTokenId(), roomId);
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        int timeLimit = updateTimeMessage.getTimeInSeconds();
        if (isAdmin == null || !isAdmin) {
            throw new MessageDeliveryException("Unauthorized: Only admin can set timer.");
        }
        timeService.setUpRoomTimer(roomId, timeLimit);
    }

    @MessageMapping("/chat/{roomId}/operateTimer")
    public void operateTimer(@Payload WebSocketMessage operateTimerMessage,
                             @DestinationVariable("roomId") String roomId,
                             SimpMessageHeaderAccessor headerAccessor) {
        //JwtUserDetails jwtUserDetails = jwtService.validateUserToken(operateTimerMessage.getTokenId(), roomId);
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        /*if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can operate timer.");
        }*/
        if (isAdmin == null || !isAdmin) {
            throw new MessageDeliveryException("Unauthorized: Only admin can operate timer.");
        }
        MessageType type = operateTimerMessage.getMessageType();

        if (type == MessageType.TIMES_UP) {
            timeService.stopRoomTimer(roomId, true);
        } else {
            timeService.stopRoomTimer(roomId, false);
        }
    }

}