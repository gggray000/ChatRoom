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
import java.util.Map;

@Controller
public class AppController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private QrCodeService qrCodeService;

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

        Room room = roomService.createRoom(roomName);
        String roomUrl = urlService.createUrl(room);

        return ResponseEntity.ok(Map.of(
                "url", roomUrl,
                "roomId", room.getId(),
                "roomName", room.getName()
        ));
    }

    @GetMapping("/admin/qrcode/{roomId}")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String roomId) {
        try {
            Room room = roomService.getRoom(roomId);
            if (room == null) {
                return ResponseEntity.notFound().build();
            }

            String roomUrl = urlService.createUrl(room);  // Use urlService here
            BufferedImage qrImage = qrCodeService.qrCodeGeneration(roomUrl, room.getName());

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
}