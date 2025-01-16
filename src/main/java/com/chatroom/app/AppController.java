package com.chatroom.app;

import com.chatroom.bot.ChatBotConfiguration;
import com.chatroom.chat.TextMessageService;
import com.chatroom.chat.WebSocketMessage;
import com.chatroom.chat.WebSocketMessageService;
import com.chatroom.room.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AppController {
    // Better use constructor injection.
    @Autowired
    private RoomService roomService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ChatBotConfiguration chatBotConfiguration;

    @Autowired
    private TimeService timeService;

    @Autowired
    private TextMessageService textMessageService;

    @Autowired
    private WebSocketMessageService webSocketMessageService;

    @GetMapping("/")
    public String home() {
        return "redirect:/admin";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin-page";
    }

    @GetMapping("/chat")
    public String enterChatRoom() {
        return "access-denied";
    }

    @GetMapping("/denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/landing")
    public String landingPage() {
        return "landing";
    }

    @PostMapping("/admin/create-room")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createChatRoom(@RequestBody Map<String, String> request) {
        String roomName = request.get("name");
        String discussionTime = request.get("timerMinutes");
        if (roomName == null || roomName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Room name is required"));
        }

        try {
            Room room = roomService.createRoom(roomName);
            String roomId = room.getRoomId();
            timeService.setTimeForRoom(roomId, Integer.parseInt(discussionTime) * 60);
            String roomUrl = urlService.createUrl(room.getRoomId());
            webSocketMessageService.createWebSocketListForRoom(roomId);
            textMessageService.createMessageListForRoom(roomId);

            Map<String, String> response = new HashMap<>();
            response.put("url", roomUrl);
            response.put("roomId", roomId);
            response.put("roomName", roomName);
            response.put("timerMinutes", Integer.toString(timeService.getTimeForRoom(roomId) / 60));
            response.put("timerSeconds", Integer.toString(timeService.getTimeForRoom(roomId) % 60));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create room: " + e.getMessage()));
        }
    }
    
    @PostMapping("/admin/cancel-room")
    @ResponseBody
    public ResponseEntity<Map<String, String>> cancelRoomCreation(@RequestBody Map<String, String> request) {
        String roomId = request.get("roomId");
        String originalName = "";
        String originalPrompt = "";
        int originalMinutes = 0;
        try{
            Room roomToBeDeleted = roomService.getRoom(roomId);
            originalName = roomToBeDeleted.getName();
            originalPrompt = roomToBeDeleted.getPrompt();
            originalMinutes = timeService.getTimeForRoom(roomId) / 60;
            this.deleteRoom(roomToBeDeleted);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete room. " + e.getMessage());
        }

        Map<String, String> response = new HashMap<>();
        response.put("originalName", originalName);
        response.put("originalPrompt", originalPrompt);
        response.put("originalMinutes", Integer.toString(originalMinutes));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/set-system-prompt")
    @ResponseBody
    public ResponseEntity<Map<String, String>> setSystemPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("systemPrompt");
        String roomId = request.get("roomId");
        chatBotConfiguration.updatePromptForRoom(roomId, prompt);
        try{
            Room temptRoom = roomService.getRoom(roomId);
            temptRoom.setPrompt(prompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set prompt for Room Entity. " + e.getMessage());
        }
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/admin/qrcode/{roomId}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String roomId, HttpServletRequest request) {
        try {
            Room room = roomService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.notFound().build();
            }
            // Get the base URL dynamically
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            // Add port only if it's not the default port (80 for HTTP or 443 for HTTPS)
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }

            String roomUrl = urlService.createUrl(roomId);  // Use urlService here
            BufferedImage qrImage = qrCodeService.qrCodeGeneration(baseUrl + roomUrl, room.getName());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/chat/{urlNumber}")
    public String enterChatRoom(@PathVariable Integer urlNumber, Model model) {
        String roomId = urlService.getRoomIdFromTable(urlNumber);
        if (roomId == null) {
            return "redirect:/denied";
        }
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            return "redirect:/denied";
        }
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", room.getName());
        model.addAttribute("timerMinutes", Integer.toString(timeService.getTimeForRoom(roomId) / 60));
        model.addAttribute("timerSeconds", Integer.toString(timeService.getTimeForRoom(roomId) % 60));
        return "chat-room";
    }

    @PostMapping("/admin/token")
    @ResponseBody
    public ResponseEntity<String> generateUserToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String roomId = request.get("roomId");
            boolean isAdmin = request.get("isAdmin") != null && Boolean.parseBoolean(request.get("isAdmin"));

            if (username == null || roomId == null) {
                return ResponseEntity.badRequest().body("Username and roomId are required");
            }

            Room room = roomService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body("Invalid room ID");
            }

            // Generate token with isAdmin flag
            String token = jwtService.generateUserToken(username, roomId, isAdmin);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error generating token: " + e.getMessage());
        }
    }

    @MessageMapping("/admin/{roomId}/shutdown")
    @SendTo("/topic/public/{roomId}")
    public WebSocketMessage shutdownChatRoom(@Payload WebSocketMessage shutdownRequest,
                                             @DestinationVariable String roomId,
                                             SimpMessageHeaderAccessor headerAccessor) {
        Boolean isAdmin = (Boolean) headerAccessor.getSessionAttributes().get("isAdmin");
        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(shutdownRequest.getTokenId(), roomId);
        if (isAdmin == null || !isAdmin || !jwtUserDetails.isAdmin()) {
            throw new MessageDeliveryException("Unauthorized: Only admin can shutdown chat room.");
        } else {
            Room roomToBeDeleted = roomService.getRoom(roomId);
            this.deleteRoom(roomToBeDeleted);
            return shutdownRequest;
        }
    }

    private void deleteRoom(Room roomTobeDeleted) {
        if (roomService.getAllRooms().containsValue(roomTobeDeleted)) {
            String roomId = roomTobeDeleted.getRoomId();
            try {
                urlService.deleteRoomUrl(roomId);
                timeService.deleteRoomTime(roomId);
                textMessageService.deleteRoomMessageHistory(roomId);
                webSocketMessageService.deleteRoomWebSocketMessageHistory(roomId);
                chatBotConfiguration.updatePromptForRoom(roomId, "");
                chatBotConfiguration.deleteRoomMemoryAndPrompt(roomId);
                roomService.deleteRoom(roomId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete room. " + e.getMessage());
            }
        }
    }

}
