package com.chatroom.bot;

import com.chatroom.chat.TextMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
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

    private void buildChatBot() {
        if (this.chatBot == null) {
            this.chatBot = AiServices.builder(ChatBot.class)
                    .chatLanguageModel(model)
                    .systemMessageProvider(memoryId -> chatBotConfiguration.prompt)
                    .build();
        }
    }

    @PostMapping("/admin/set-system-prompt")
    @ResponseBody
    public ResponseEntity<Map<String, String>> setSystemPrompt(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        chatBotConfiguration.updatePrompt(prompt);
        buildChatBot();
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("http://localhost:11434/api/chat")
    public String chatBot(List<TextMessage> messageList) {
//        String messageAsString = parseTextMessages(messageList);
        return chatBot.summarize(messageList.toString());
    }
}
