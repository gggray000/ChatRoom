package com.chatroom.bot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;

@AiService
public interface ChatBot {
//    @SystemMessage({
//            "You are an AI assistant that helps summarize chat discussions. " +
//                    "When provided with a chat history, create a concise summary " +
//                    "highlighting the main points of the discussion and key contributions from participants. " +
//                    "Focus on the key topics discussed and important points made by each participant. " +
//                    "Keep the summary clear and organized."
//    })
    String summarize(String messageAsString);
}