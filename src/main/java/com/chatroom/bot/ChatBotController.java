package com.chatroom.bot;

import com.chatroom.chat.TextMessage;
import com.chatroom.chat.TextMessageService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ChatBotController {

    private final ChatLanguageModel model;
    private final TextMessageService textMessageService;
    private ChatBot chatBot;
    private final ChatBotConfiguration chatBotConfiguration;

    public ChatBotController(ChatLanguageModel model, ChatBotConfiguration configuration, TextMessageService textMessageService) {
        this.model = model;
        this.chatBotConfiguration = configuration;
        this.textMessageService = textMessageService;
    }

    public String makeSummary(String roomId) {
        ChatMemory roomMemory = chatBotConfiguration.getOrCreateMemoryForRoom(roomId);
        String systemPrompt = chatBotConfiguration.getPromptForRoom(roomId);

        // Create a one-time use ChatBot instance with the room-specific memory
        this.chatBot = AiServices.builder(ChatBot.class)
                .chatLanguageModel(model)
                .chatMemory(roomMemory)
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return chatBot.summarize(textMessageService.exportStoredMessages(roomId));
    }
}
