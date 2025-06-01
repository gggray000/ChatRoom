package com.chatroom.bot;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class ChatBotConfiguration {

    private final Map<String, String> roomPrompts = new ConcurrentHashMap<>();
    private final Map<String, ChatMemory> roomMemories = new ConcurrentHashMap<>();

    private final String PROMPT_TEMPLATE =
            """
            You are an AI assistant that helps summarize chat discussions, based on the main topic
            provided by admin.
            When provided with a chat history, make sure to follow the following guidelines:
            1. Output the summary with MarkDown format.
            2. Summary should have four sections: (1) Main Topic, (2) Main Points, (3) Key contributions from Participants, (4) Decision or Follow-ups.
            3. In the "Main Topic" part, summarize the main topic set by admin in a few words in one line.
            4. In the "Main Points" part, create a concise summary listing the main points of the discussion.
            5. In the "Key Contributions from Participants" part, identify and mention key contributions from participants.
            6. In the "Decision or Follow-ups" part, highlight any decisions or follow-up actions that were commonly agreed upon.
            7. If there were any unresolved questions or topics, mention them.
            8. Seperate the four sections with blank new line. Begin each section with a new line.
            9. Use markdown strong (bold text) for important points.
            10. Always say "Thank you for your participation!" at the end of summary, add a new blank line before this sentence.
                    
            Main Topic from Admin:
            %s
                    """;

    public ChatMemory getChatMemoryForRoom(String roomId) {
        return roomMemories.get(roomId);
    }

    public void createMemoryForRoom(String roomId) {
        roomMemories.put(roomId, MessageWindowChatMemory.withMaxMessages(500));
    }

    public String getPromptForRoom(String roomId) {
        return roomPrompts.getOrDefault(roomId, String.format(PROMPT_TEMPLATE, ""));
    }

    public void updatePromptForRoom(String roomId, String newPrompt) {
        roomPrompts.put(roomId, String.format(PROMPT_TEMPLATE, newPrompt != null ? newPrompt.trim() : ""));
    }

    public void deleteRoomMemoryAndPrompt(String roomId){
        roomMemories.remove(roomId);
        roomPrompts.remove(roomId);
    }
}
