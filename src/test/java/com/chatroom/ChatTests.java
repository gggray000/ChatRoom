package com.chatroom;

import com.chatroom.chat.TextMessage;
import com.chatroom.chat.TextMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatTests {
    private TextMessageService textMessageService;

    @BeforeEach
    void setUp() {
        textMessageService = new TextMessageService();
    }

    @Test
    void testTextMessageExport() {
        TextMessage msg1 = new TextMessage("a", "001", "Hi!");
        TextMessage msg2 = new TextMessage("b", "001", "Hello!!!");
        textMessageService.saveTextMessage(msg1);
        textMessageService.saveTextMessage(msg2);
        assertEquals(2, textMessageService.messageList.size());
        TextMessage msg3 = new TextMessage("c", "002", "Hello from another room!");
        System.out.println(textMessageService.exportStoredMessages("001"));
        assertEquals(2, textMessageService.exportStoredMessages("001").lines().count());

    }
}
