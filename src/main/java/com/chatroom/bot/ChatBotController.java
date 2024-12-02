package com.chatroom.bot;

import com.chatroom.chat.TextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ChatBotController {

    @Autowired
    ChatBot chatBot;

    public ChatBotController(ChatBot chatBot) {
        this.chatBot = chatBot;
    }

    @PostMapping("http://localhost:11434/api/chat")
    public String chatBot(List<TextMessage> messageList) {
//        String messageAsString = parseTextMessages(messageList);
        return chatBot.summarize(messageList.toString());
    }
}
