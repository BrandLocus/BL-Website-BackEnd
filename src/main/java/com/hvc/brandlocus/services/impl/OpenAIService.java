package com.hvc.brandlocus.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hvc.brandlocus.dto.request.MessageClassification;
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

    public String getResponseWithHistory(
            List<ChatMessage> chatHistory,
            String userInput,
            String businessBrief,
            String businessName,
            String industryName,
            String userName
    ) {
        try {
            log.info("Calling OpenAI with {} history messages", chatHistory.size());

            // Build the personalization context
            String personalizationContext = """
            You are a helpful assistant for BrandLocus, a business/marketing platform.

            PERSONALIZATION CONTEXT:
            - User Name: %s
            - Business Name: %s
            - Industry: %s
            - Business Brief: %s

            IMPORTANT: Use the above information to personalize all responses. 
            Do NOT give generic answers. Tailor insights, suggestions, and recommendations to this user and their business.
            
            Only answer questions related to business, marketing, branding, and product management.
            If a user asks about unrelated topics (politics, sports, cooking, etc.), politely decline and redirect them to business-related topics.
            Always be professional and helpful.
        """.formatted(
                    userName != null ? userName : "N/A",
                    businessName != null ? businessName : "N/A",
                    industryName != null ? industryName : "N/A",
                    businessBrief != null ? businessBrief : "N/A"
            );

            ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                    .addDeveloperMessage(personalizationContext)
                    .model(ChatModel.GPT_4O_MINI);

            // Add chat history
            for (ChatMessage msg : chatHistory) {
                if (msg.getSender() == SenderType.USER) {
                    paramsBuilder.addUserMessage(msg.getContent());
                } else {
                    paramsBuilder.addAssistantMessage(msg.getContent());
                }
            }

            // Add current user input
            paramsBuilder.addUserMessage(userInput);

            ChatCompletion chatCompletion = openAIClient.chat().completions().create(paramsBuilder.build());

            return chatCompletion.choices().get(0).message().content().orElse("");

        } catch (Exception e) {
            log.error("Failed to get response from OpenAI: {}", e.getMessage(), e);
            throw new OpenAIException("AI service temporarily unavailable. Please try again later.", e);
        }
    }





    public MessageClassification classifyMessage(String text) {
        try {
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_4O_MINI)
                    .addSystemMessage("""
                    You are a classifier. Return ONLY a JSON object:
                    {
                      "sector": "<category>",
                      "keywords": ["kw1","kw2","kw3"],
                      "topic": "<one-word topic>"
                    }
                    
                    Valid sectors:

                    # Business Functional Sectors
                    - Branding
                    - Marketing
                    - Advertising
                    - Social Media Marketing
                    - Sales
                    - Customer Experience
                    - Product Management
                    - Product Design
                    - Content Strategy
                    - Business Strategy
                    - Competitive Analysis
                    - Operations
                    - Human Resources
                    - Data Analytics
                    - Finance & Accounting
                    - Pricing Strategy
                    - Market Research
                    - Project Management
                    - Innovation
                    - Growth Strategy
                    - Customer Support
                    - Leadership

                    # Industry Sectors
                    - Retail
                    - E-commerce
                    - FMCG
                    - Technology
                    - Software
                    - SaaS
                    - Fintech
                    - Cybersecurity
                    - AI / Machine Learning
                    - Healthcare
                    - Pharmaceuticals
                    - Wellness
                    - Logistics
                    - Transportation
                    - Supply Chain
                    - Agriculture
                    - Agritech
                    - Food Production
                    - Media & Entertainment
                    - Gaming
                    - Education
                    - Real Estate
                    - Manufacturing
                    - Energy
                    - Oil & Gas
                    - Renewable Energy
                    - Hospitality
                    - Travel & Tourism
                    - Legal
                    - Nonprofit
                    - Government
                    - Fashion
                    - Beauty

                    - Off-topic
                    """)
                    .addUserMessage(text)
                    .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);
            String json = completion.choices().getFirst().message().content().orElse("{}").trim();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, MessageClassification.class);

        } catch (Exception e) {
            log.error("Failed to classify message: {}", e.getMessage(), e);

            // Fallback to prevent nulls
            MessageClassification fallback = new MessageClassification();
            fallback.setSector("Unknown");
            fallback.setKeywords(List.of());
            fallback.setTopic("Unknown");
            return fallback;
        }
    }
    }





