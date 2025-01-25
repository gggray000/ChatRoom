package com.chatroom.bot;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;

public class TestOllamaChatBot {

    public static void main(String[] args) {

        StreamingChatLanguageModel model = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("deepseek-r1:14b")
                .build();

        String userMessage = "Provide 3 short bullet points explaining why Java is awesome";

        model.generate(userMessage, new StreamingResponseHandler<AiMessage>() {

            @Override
            public void onNext(String token) {
                System.out.print(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                System.out.println("\n");
                System.out.println("Completed");
                System.exit(0);
            }

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });

    }
}