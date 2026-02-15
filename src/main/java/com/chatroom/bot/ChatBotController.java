package com.chatroom.bot;

import com.chatroom.chat.TextMessageService;
import com.chatroom.chat.WebSocketMessageService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatBotController {

    private final ChatLanguageModel model;
    private final WebSocketMessageService webSocketMessageService;
    private ChatBot chatBot;
    private final ChatBotConfiguration chatBotConfiguration;

    public ChatBotController(ChatLanguageModel model, ChatBotConfiguration configuration, TextMessageService textMessageService, WebSocketMessageService webSocketMessageService) {
        this.model = model;
        this.chatBotConfiguration = configuration;
        this.webSocketMessageService = webSocketMessageService;
    }

    public String makeSummary(String roomId) {
        chatBotConfiguration.createMemoryForRoom(roomId);
        String systemPrompt = chatBotConfiguration.getPromptForRoom(roomId);

        // Create a one-time use ChatBot instance with the room-specific memory
        this.chatBot = AiServices.builder(ChatBot.class)
                .chatLanguageModel(model)
                .chatMemory(chatBotConfiguration.getChatMemoryForRoom(roomId))
                .systemMessageProvider(memoryId -> systemPrompt)
                .build();

        return chatBot.summarize(webSocketMessageService.messageHistoryToString(roomId));
    }
}
