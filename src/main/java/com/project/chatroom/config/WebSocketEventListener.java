// Need to refactor this to another package
package com.project.chatroom.config;

import com.project.chatroom.chat.ChatMessage;
import com.project.chatroom.chat.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

//@RequiredArgsConstructor is a Lombok annotation that generates a constructor for all final fields in the class.
// It’s a shorthand for defining dependency injection without manually writing the constructor.
//This is another Lombok annotation that provides a "Simple Logging Facade for Java" logger instance.
//With this, Lombok automatically creates a private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class); statement.
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;

    @EventListener
    public void HandleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null){
            log.info("User disconnected: {}", username);
            var chatMessage = ChatMessage.builder()
                    .messageType(MessageType.LEAVE)
                    .sender(username)
                    .build();
            //inform the others a user has left
            messageTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }

/*    @EventListener
    public void HandleWebSocketJoinListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if(username != null){
            log.info("User disconnected: {}", username);
            var chatMessage = ChatMessage.builder()
                    .messageType(MessageType.JOIN)
                    .sender(username)
                    .build();
            //inform the others a user has left
            messageTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }*/

    // need join and sent message?


}
