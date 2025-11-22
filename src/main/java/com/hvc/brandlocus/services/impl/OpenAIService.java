package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.enums.SenderType;
import com.hvc.brandlocus.exception.OpenAIException;
import com.openai.client.OpenAIClient;

import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final OpenAIClient openAIClient;

    public String getResponse(String userInput) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addUserMessage(userInput)
                .model(ChatModel.GPT_4O_MINI)
                .build();

        ChatCompletion chatCompletion = openAIClient.chat().completions().create(params);

        return chatCompletion.choices().getFirst().message().content().orElse("");
    }

    public String getResponseWithHistory(List<ChatMessage> chatHistory, String userInput) {
        try {
            log.info("Calling OpenAI with {} history messages", chatHistory.size());

            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                    .addDeveloperMessage("""
                    You are a helpful assistant for BrandLocus, a business/marketing platform.
                    
                    IMPORTANT RULES:
                    - Only answer questions related to business, marketing, branding, and product management.
                    - If a user asks about unrelated topics (politics, sports, cooking, etc.), politely decline and redirect them to business-related topics.
                    - Always be professional and helpful.
                    - Remember the conversation history and user details.
                    
                    Example responses for off-topic questions:
                    - "I'm focused on helping with business and marketing topics. Is there anything related to your brand or business I can help with?"
                    - "That's outside my area of expertise. I'm here to help with branding, marketing, and business strategies. How can I assist you with those?"
                    """)
                    .model(ChatModel.GPT_4O_MINI);

            for (ChatMessage msg : chatHistory) {
                if (msg.getSender() == SenderType.USER) {
                    paramsBuilder.addUserMessage(msg.getContent());
                } else {
                    paramsBuilder.addAssistantMessage(msg.getContent());
                }
            }

            paramsBuilder.addUserMessage(userInput);

            ChatCompletion chatCompletion = openAIClient.chat().completions().create(paramsBuilder.build());

            return chatCompletion.choices().get(0).message().content().orElse("");

        } catch (Exception e) {
            log.error("Failed to get response from OpenAI: {}", e.getMessage(), e);
            throw new OpenAIException("AI service temporarily unavailable. Please try again later.", e);
        }
    }

//    public String getResponseWithHistory(List<ChatMessage> chatHistory, String userInput) {
//        System.out.println("!!! HARDCODE TEST METHOD CALLED !!!");  // Add this
//
//        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
//                .addDeveloperMessage("You are a helpful assistant.")
//                .model(ChatModel.GPT_4O_MINI);
//
//        paramsBuilder.addUserMessage("My name is David and I am from Nigeria");
//        paramsBuilder.addAssistantMessage("Nice to meet you David from Nigeria!");
//
//        paramsBuilder.addUserMessage(userInput);
//
//        ChatCompletion chatCompletion = openAIClient.chat().completions().create(paramsBuilder.build());
//
//        return chatCompletion.choices().get(0).message().content().orElse("");
//    }


}