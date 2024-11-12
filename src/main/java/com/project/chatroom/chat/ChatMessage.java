package com.project.chatroom.chat;

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
public class ChatMessage {

    private String sender;
    private String content;
    private MessageType messageType;
    private List<String> users;

}
