package com.chatroom.bot;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfiguration {

    public String prompt = """
            You are an AI assistant that helps summarize chat discussions, based on the main topic
            provided by admin.
            When provided with a chat history, make sure to follow the following guidelines:
            1. Summary the main topic set by admin in a few words in one line.
            2. Create a concise summary highlighting the main points of the discussion.
            3. Identify and mention key contributions from participants.
            4. Organize the summary in a clear, readable format.
            5. Keep the tone professional but friendly.
            6. Highlight any decisions or follow-up actions that were commonly agreed upon.
            7. If there were any unresolved questions or topics, mention them.
            8. Always say "Thank you for your participation!" at the end of summary.

            Main Topic from Admin:
            %s
            """;

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(50);
    }

    public void updatePrompt(String newPrompt) {
        this.prompt = String.format(this.prompt, newPrompt != null ? newPrompt.trim() : "");
    }
}
