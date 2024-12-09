package com.chatroom.bot;

import com.chatroom.chat.TextMessage;
import com.chatroom.chat.TextMessageService;
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

    // Even though the bot is built by @PostConstruct, the systemMessageProvide holds a reference to prompt
    // It can get updates of the prompt.
    @PostConstruct
    private void buildChatBot() {
            this.chatBot = AiServices.builder(ChatBot.class)
                    .chatLanguageModel(model)
                    .systemMessageProvider(memoryId -> chatBotConfiguration.prompt)
                    .build();
    }

    public String makeSummary(String roomId) {
        return chatBot.summarize(textMessageService.exportStoredMessages(roomId));
    }
}
