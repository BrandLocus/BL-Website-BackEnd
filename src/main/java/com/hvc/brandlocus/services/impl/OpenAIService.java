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
            String userName,
            String state,
            String country
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
- State/Region: %s
- Country: %s

IMPORTANT:
- Use the location (state and country) to tailor marketing strategies, 
  customer behavior insights, pricing, channels, and regulations.
- Avoid generic advice. Always localize recommendations.
- Reference local trends, consumer behavior, and market realities when relevant.

SCOPE RULES:
- Only answer questions related to business, marketing, branding, product management, and growth.
- If the user asks about unrelated topics (politics, sports, cooking, etc.), politely decline and redirect to business-related topics.
- Always be professional, concise, and actionable.
""".formatted(
                    userName != null && !userName.isBlank() ? userName : "N/A",
                    businessName != null && !businessName.isBlank() ? businessName : "N/A",
                    industryName != null && !industryName.isBlank() ? industryName : "N/A",
                    businessBrief != null && !businessBrief.isBlank() ? businessBrief : "N/A",
                    state != null && !state.isBlank() ? state : "N/A",
                    country != null && !country.isBlank() ? country : "N/A"
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
                You are a strict classifier.

                Return ONLY a valid JSON object in this exact format:
                {
                  "sector": "<ONE_SECTOR_FROM_LIST>",
                  "keywords": ["kw1", "kw2", "kw3"],
                  "topic": "<one-word topic>"
                }

                VALID SECTORS (use ONLY one of these):
                - Agriculture
                - Manufacturing
                - Trade
                - Energy & Climate
                - Technology
                - Healthcare
                - Real Estate
                - Transport & Logistics
                - Education
                - Creative Media & Culture
                - Finance
                - Public Policy
                - Others

                CLASSIFICATION RULES:
                - Pick ONE dominant sector only.
                - If the message touches multiple sectors, choose the most relevant.
                - If it is business-related but unclear, use "Others".
                - If it is NOT business-related, use "Others".
                - Do NOT invent new sectors.
                - Do NOT return explanations, markdown, or extra text.
                """)
                    .addUserMessage(text)
                    .build();

            ChatCompletion completion =
                    openAIClient.chat().completions().create(params);

            String json = completion.choices()
                    .getFirst()
                    .message()
                    .content()
                    .orElse("{}")
                    .trim();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, MessageClassification.class);

        } catch (Exception e) {
            log.error("Failed to classify message: {}", e.getMessage(), e);

            // HARD fallback – never break your pipeline
            MessageClassification fallback = new MessageClassification();
            fallback.setSector("Others");
            fallback.setKeywords(List.of());
            fallback.setTopic("general");
            return fallback;
        }
    }






    /*public MessageClassification classifyMessage(String text) {
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
    }*/
    }





