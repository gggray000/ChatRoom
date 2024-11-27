package com.chatroom.bot;

import dev.langchain4j.data.message.AiMessage;

import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.output.Response;

import java.awt.*;

public class TestOllamaChatBot {

//    @AiService
//    interface Assistant {
//        // TODO set up a @SystemMessage provider
//        @SystemMessage("You are a professor teaching java, you like to explain java in a funny way.")
//        String chat(String message);
//    }

    public static void main(String[] args) {


//        ChatLanguageModel model =
//                OllamaChatModel.builder()
//                        .baseUrl("http://localhost:11434")
//                        .modelName("llama3.2")
//                        .build();
//
//        String answer = model.generate("Provide 3 short bullet points explaining why Java is awesome");
//        System.out.println(answer);

        StreamingChatLanguageModel model = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .build();

//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .maxMessages(10)
//                .chatMemoryStore(new PersistentChatMemoryStore())
//                .build();
//
//        Assistant assistant = AiServices.builder(Assistant.class)
//                .streamingChatLanguageModel(model)
//                .chatMemory(chatMemory)
//                .build();

        // TODO
        // .builder().format("json")
        // return a chat message in json form, send it to the chat room.
        // https://docs.langchain4j.dev/tutorials/ai-services

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