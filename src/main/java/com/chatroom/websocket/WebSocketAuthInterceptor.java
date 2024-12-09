package com.chatroom.websocket;

import com.chatroom.room.JwtService;
import com.chatroom.room.JwtUserDetails;
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
            String token = accessor.getFirstNativeHeader("token");
            String roomId = accessor.getFirstNativeHeader("roomId");

            if (token != null) {
                JwtUserDetails userDetails = jwtService.validateUserToken(token);
                if (userDetails == null || !roomId.equals(userDetails.getRoomId())) {
                    throw new MessageDeliveryException("Invalid token");
                }
                // Set user details in session attributes
                accessor.getSessionAttributes().put("username", userDetails.getUsername());
                accessor.getSessionAttributes().put("tokenId", userDetails.getTokenId());
                accessor.getSessionAttributes().put("roomId", roomId);
                accessor.getSessionAttributes().put("isAdmin", userDetails.isAdmin());
            }
        }
        return message;
    }
}