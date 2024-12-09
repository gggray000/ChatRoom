package com.chatroom.bot;

import com.chatroom.chat.TextMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class ChatBotController {

    private final ChatLanguageModel model;
    private ChatBot chatBot;
    private final ChatBotConfiguration chatBotConfiguration;

    public ChatBotController(ChatLanguageModel model, ChatBotConfiguration configuration) {
        this.model = model;
        this.chatBotConfiguration = configuration;
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

    public String makeSummary(List<TextMessage> messageList) {
        return chatBot.summarize(messageList.toString());
    }
}
