package com.chatroom.websocket;

import com.chatroom.chat.ChatController;
import com.chatroom.chat.MessageType;
import com.chatroom.chat.WebSocketMessage;
import com.chatroom.room.JwtService;
import com.chatroom.room.JwtUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ChatController chatController;
    private final SimpMessageSendingOperations messagingTemplate;
    private final JwtService jwtService;

    @Autowired
    public WebSocketEventListener(ChatController chatController, SimpMessageSendingOperations messagingTemplate, JwtService jwtService) {
        this.chatController = chatController;
        this.messagingTemplate = messagingTemplate;
        this.jwtService = jwtService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
        String tokenId = (String) headerAccessor.getSessionAttributes().get("tokenId");

        JwtUserDetails jwtUserDetails = jwtService.validateUserToken(tokenId, roomId);
        if (jwtUserDetails.isAdmin()) {
            handleDisconnectionAndTimer(roomId);
        }
        if (username != null && roomId != null) {
            WebSocketMessage leaveMessage = WebSocketMessage.builder()
                    .messageType(MessageType.LEAVE)
                    .sender(username)
                    .tokenId(tokenId)  // Include tokenId in leave message
                    .build();
            messagingTemplate.convertAndSend("/topic/public/" + roomId, leaveMessage);
            logger.info("User Disconnected: " + username);
            chatController.removeUser(username, roomId);
        }
    }

    private void handleDisconnectionAndTimer(String roomId) {
        WebSocketMessage pauseTimerMessage = WebSocketMessage.builder()
                .messageType(MessageType.TIMER_PAUSE)
                .build();
        messagingTemplate.convertAndSend("/topic/public/" + roomId, pauseTimerMessage);

    }
}