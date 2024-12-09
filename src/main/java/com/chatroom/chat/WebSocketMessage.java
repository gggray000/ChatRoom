package com.chatroom.chat;

import lombok.*;

import java.util.List;
import java.util.Map;

/*@Builder enables to construct objects like this:
* User user = User.builder()
                .name("Alice")
                .age(25)
                .email("alice@example.com")
                .build();
* */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketMessage {

    private String sender;
    private String content;
    private MessageType messageType;
    private List<Map<String, Object>> users;
    private String tokenId;
    private String resource;

}
