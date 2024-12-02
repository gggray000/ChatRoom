package com.chatroom.bot;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfiguration {
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(50);
    }

}
