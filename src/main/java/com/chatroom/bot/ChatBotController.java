package com.chatroom.bot;

import com.chatroom.chat.TextChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.chatroom.bot.ChatBotTools.parseTextChatMessages;

@RestController
public class ChatBotController {

    @Autowired
    ChatBot chatBot;

    public ChatBotController(ChatBot chatBot) {
        this.chatBot = chatBot;
    }

    @PostMapping("http://localhost:11434/api/chat")
    public String chatBot(List<TextChatMessage> messageList) {
//        String messageAsString = parseTextChatMessages(messageList);
        return chatBot.summarize(messageList.toString());
    }
}
