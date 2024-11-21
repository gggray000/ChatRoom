package com.chatroom.bot;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

public class TestOllamaChatBot {

    public static void main(String[] args) {

        ChatLanguageModel model =
                OllamaChatModel.builder()
                        .baseUrl("http://localhost:11434")
                        .modelName("llama3.2")
                        .build();

        String answer = model.generate("Provide 3 short bullet points explaining why Java is awesome");
        System.out.println(answer);

    }
}