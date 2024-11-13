// Need to refactor this to another package
package com.chatroom.websocket;

import com.chatroom.chat.ChatController;
import com.chatroom.chat.ChatMessage;
import com.chatroom.chat.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;

//@RequiredArgsConstructor is a Lombok annotation that generates a constructor for all final fields in the class.
// It’s a shorthand for defining dependency injection without manually writing the constructor.
//This is another Lombok annotation that provides a "Simple Logging Facade for Java" logger instance.
//With this, Lombok automatically creates a private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class); statement.
@Component
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatController chatController;

    @Autowired
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                  ChatController chatController) {
        this.messageTemplate = messagingTemplate;
        this.chatController = chatController;
    }

    @EventListener
    public void HandleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor
                            .getSessionAttributes()
                            .get("username");
        if(username != null){
            log.info("User disconnected: {}", username);
            messageTemplate.convertAndSend("/topic/public",
                    ChatMessage.builder()
                            .messageType(MessageType.LEAVE)
                            .sender(username)
                            .build()
            );

            // Remove user and send updated list
            chatController.getConnectedUsers().remove(username);
            messageTemplate.convertAndSend("/topic/public",
                    ChatMessage.builder()
                            .messageType(MessageType.USER_LIST)
                            .users(new ArrayList<>(chatController.getConnectedUsers()))
                            .build()
            );
        }
    }

}
