package com.chatroom.bot;

import com.chatroom.chat.TextMessage;

import java.util.List;

// Pending to implement for now.

//@Component
public class ChatBotTools {
    //@Tool
    public static String parseTextMessages(List<TextMessage> messages) {
        StringBuilder chatHistory = new StringBuilder();
        if (messages != null) {
            for (TextMessage message : messages) {
                if (message != null && message.getUserName() != null && message.getContent() != null) {
                    chatHistory.append(message).append("\n");
                }
            }
        }
        return chatHistory.toString();
    }
}
