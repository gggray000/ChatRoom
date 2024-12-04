package com.chatroom.websocket;

import com.chatroom.room.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String adminToken = accessor.getFirstNativeHeader("adminToken");
            String roomId = accessor.getFirstNativeHeader("roomId");

            if (adminToken != null && roomId != null) {
                // Use JwtService to validate token
                boolean isValid = jwtService.validateToken(adminToken, roomId);

                if (!isValid) {
                    throw new MessageDeliveryException("Invalid admin token for room: " + roomId);
                }
                // If valid, set user principal
                accessor.setUser(new Principal() {
                    @Override
                    public String getName() {
                        return roomId;
                    }
                });
            }
        }
        return message;
    }
}