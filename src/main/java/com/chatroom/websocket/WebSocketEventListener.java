package com.chatroom.websocket;

import com.chatroom.chat.ChatController;
import com.chatroom.chat.ChatMessage;
import com.chatroom.chat.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final ChatController chatController;
    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(ChatController chatController, SimpMessageSendingOperations messagingTemplate) {
        this.chatController = chatController;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        var headerAccessor = org.springframework.messaging.simp.stomp.StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        if (username != null && roomId != null) {
            logger.info("User Disconnected: " + username);
            chatController.removeUser(username, roomId);
        }
    }
}