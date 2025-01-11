package com.chatroom.chat;

import lombok.*;

import java.util.List;

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
    private List<String> userList;
    private String tokenId;
    private String resource;
    private int timeInSeconds;

}
