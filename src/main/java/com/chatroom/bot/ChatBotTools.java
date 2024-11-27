package com.chatroom.bot;

import com.chatroom.chat.TextChatMessage;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
public class ChatBotTools {
    //@Tool
    public static String parseTextChatMessages(List<TextChatMessage> messages) {
        StringBuilder chatHistory = new StringBuilder();
        if (messages != null) {
            for (TextChatMessage message : messages) {
                if (message != null && message.getUserName() != null && message.getContent() != null) {
                    chatHistory.append(message).append("\n");
                }
            }
        }
        return chatHistory.toString();
    }
}
