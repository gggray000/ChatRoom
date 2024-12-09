package com.chatroom.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.chatroom.room.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AppController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin-page";
    }

    @GetMapping("/chat")
    public String enterChatRoom() {
        return "chat-room";
    }

    @GetMapping("/denied")
    public String accessDenied() {
        return "access-denied";
    }

    @PostMapping("/admin/create-room")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createChatRoom(@RequestBody Map<String, String> request) {
        String roomName = request.get("name");
        if (roomName == null || roomName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Room name is required"));
        }

        try {
            // Create room
            Room room = roomService.createRoom(roomName);
            String roomUrl = urlService.createUrl(room);

            // Generate admin token
            String adminToken = jwtService.generateAdminToken(room.getId());
            room.setAdminToken(adminToken);

            // Create response
            Map<String, String> response = new HashMap<>();
            response.put("url", roomUrl);
            response.put("roomId", room.getId());
            response.put("roomName", room.getName());
            response.put("adminToken", adminToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create room: " + e.getMessage()));
        }
    }

    @GetMapping("/admin/qrcode/{roomId}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.notFound().build();
            }

            String roomUrl = urlService.createUrl(room);  // Use urlService here
            BufferedImage qrImage = qrCodeService.qrCodeGeneration("http://localhost:8080" + roomUrl, room.getName());

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

    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable String roomId, Model model) {
        Room room = roomService.getRoom(roomId);
        if (room == null) {
            return "redirect:/denied";
        }
        model.addAttribute("roomId", roomId);
        model.addAttribute("roomName", room.getName());
        return "chat-room";
    }

    @PostMapping("/api/auth/token")
    @ResponseBody
    public ResponseEntity<String> generateUserToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String roomId = request.get("roomId");

            if (username == null || roomId == null) {
                return ResponseEntity.badRequest().body("Username and roomId are required");
            }

            // Verify room exists
            Room room = roomService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.badRequest().body("Invalid room ID");
            }

            // Generate user token
            String token = jwtService.generateUserToken(username, roomId);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error generating token: " + e.getMessage());
        }
    }


}