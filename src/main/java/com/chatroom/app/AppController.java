package com.chatroom.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.chatroom.room.*;

import java.util.Map;

@Controller
public class AppController {

    @Autowired
    private RoomService roomService;

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
        Room room = roomService.createRoom(request.get("name"));
        String roomUrl = "/chat/" + room.getId();
        return ResponseEntity.ok(Map.of("url", roomUrl));
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