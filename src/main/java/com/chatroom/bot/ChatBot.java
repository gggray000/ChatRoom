package com.chatroom.bot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;

@AiService
public interface ChatBot {
    String summarize(String messageAsString);
}