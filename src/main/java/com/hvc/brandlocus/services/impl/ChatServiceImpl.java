package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.ChatMessageRequest;
import com.hvc.brandlocus.dto.request.EditAIResponseRequest;
import com.hvc.brandlocus.dto.request.MessageClassification;
import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.ChatMessageResponse;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.dto.response.SessionResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.entities.ChatSession;
import com.hvc.brandlocus.enums.ChatStatus;
import com.hvc.brandlocus.enums.ChatType;
import com.hvc.brandlocus.enums.SenderType;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.ChatMessageRepository;
import com.hvc.brandlocus.repositories.ChatSessionRepository;
import com.hvc.brandlocus.services.AIChatService;
import com.hvc.brandlocus.utils.ApiResponse;
import com.hvc.brandlocus.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements AIChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final BaseUserRepository baseUserRepository;

    private final OpenAIService openAIService;


    @Override
    public ResponseEntity<ApiResponse<?>> startChat(Principal principal, ChatMessageRequest request) {
        try {
            log.info("Starting chat with the AI");

            BaseUser user = baseUserRepository.findByEmail(principal.getName().trim())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));


            user.setChatRequestCount(user.getChatRequestCount() + 1);

            boolean milestoneReached = user.getChatRequestCount() % 5 == 0;

            // Saving here triggers optimistic locking
            baseUserRepository.save(user);

//            String businessBrief = user.getProfile() != null ? user.getProfile().getBusinessBrief() : "";


            ChatSession session;
            if (request.getSessionId() == null) {
                session = chatSessionRepository.save(
                        ChatSession.builder()
                                .user(user)
                                .title(request.getTitle() != null ? request.getTitle() : "New Chat")
                                .build()
                );
            } else {
                session = chatSessionRepository.findById(request.getSessionId())
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));
            }

            List<ChatMessage> chatHistory = chatMessageRepository.findByChatSessionOrderByCreatedAtAsc(session);


            ChatMessage userMessage = chatMessageRepository.save(
                    ChatMessage.builder()
                            .chatSession(session)
                            .sender(SenderType.USER)
                            .chatType(ChatType.PROMPT)
                            .content(request.getContent())
                            .build()
            );

            log.info("User message saved, session ID: {}", session.getId());


            String aiAnswer = openAIService.getResponseWithHistory(
                    chatHistory,
                    request.getContent(),
                    user.getProfile() != null ? user.getProfile().getBusinessBrief() : "",
                    user.getProfile() != null ? user.getProfile().getBusinessName() : "",
                    user.getProfile() != null ? user.getProfile().getIndustryName() : "",
                    user.getFirstName() + " " + user.getLastName()
            );


            MessageClassification classification = openAIService.classifyMessage(aiAnswer);


            ChatMessage aiMessage = chatMessageRepository.save(
                    ChatMessage.builder()
                            .chatSession(session)
                            .sender(SenderType.AI)
                            .chatType(ChatType.PROMPT_RESPONSE)
                            .content(aiAnswer)
                            .sector(classification.getSector())
                            .keywords(String.join(", ", classification.getKeywords()))
                            .topic(classification.getTopic())
                            .build()
            );

            log.info("AI message saved with sector '{}' and keywords {}", classification.getSector(), classification.getKeywords());

            // -------------------------------
            ChatMessageResponse userResponse = ChatMessageResponse.builder()
                    .sessionId(session.getId())
                    .messageId(userMessage.getId())
                    .userType(userMessage.getSender().toString())
                    .chatType(userMessage.getChatType().toString())
                    .content(userMessage.getContent())
                    .createdAt(userMessage.getCreatedAt().toString())
                    .build();

            ChatMessageResponse aiResponse = ChatMessageResponse.builder()
                    .sessionId(session.getId())
                    .messageId(aiMessage.getId())
                    .userType(aiMessage.getSender().toString())
                    .chatType(aiMessage.getChatType().toString())
                    .content(aiMessage.getContent())
                    .name(user.getFirstName() + " " + user.getLastName())
                    .industryName(user.getIndustryName())
                    .businessName(user.getBusinessName())
                    .createdAt(aiMessage.getCreatedAt().toString())
                    .build();

//            return ResponseEntity.ok(
//                    createSuccessResponse(List.of(userResponse, aiResponse), "Message processed successfully")
//            );

            return ResponseEntity.ok(
                    createSuccessResponse(
                            Map.of(
                                    "messages", List.of(userResponse, aiResponse),
                                    "milestone", milestoneReached
                            ),
                            "Message processed successfully"
                    )
            );


        } catch (Exception ex) {
            log.error("Failed to process chat", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(ex.getLocalizedMessage(), "Failed to process chat"));
        }
    }


    @Override
    public ResponseEntity<ApiResponse<?>> getChats(
            Principal principal,
            Long sessionId,
            PaginationRequest paginationRequest
    ) {
        try {
            log.info("session id :{}", sessionId);
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
            }

            BaseUser user = optionalUser.get();
            boolean isAdmin = UserRoles.ADMIN.getValue().equalsIgnoreCase(user.getRole().getName());

            Sort.Direction direction = paginationRequest.getOrder().equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(
                    paginationRequest.getPage(),
                    paginationRequest.getLimit(),
                    Sort.by(direction, paginationRequest.getSortBy())
            );

            // ================================
            //       IF SESSION ID PROVIDED
            // ================================
            if (sessionId != null) {

                ChatSession session = chatSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));

                // 🔒 If not admin, enforce ownership check
                if (!isAdmin && !session.getUser().getId().equals(user.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(createFailureResponse("Unauthorized", "You do not have access to this session"));
                }

                Page<ChatMessage> messagePage = chatMessageRepository.findAllByChatSessionId(sessionId, pageable);

                List<ChatMessageResponse> messages = messagePage.getContent().stream()
                        .map(msg -> ChatMessageResponse.builder()
                                .sessionId(session.getId())
                                .messageId(msg.getId())
                                .userType(msg.getSender().toString())
                                .chatType(msg.getChatType().toString())
                                .content(msg.getContent())
                                .createdAt(msg.getCreatedAt().toString())
                                .build())
                        .toList();

                PaginationResponse<ChatMessageResponse> response = PaginationResponse.<ChatMessageResponse>builder()
                        .content(messages)
                        .page(messagePage.getNumber())
                        .size(messagePage.getSize())
                        .totalElements(messagePage.getTotalElements())
                        .totalPages(messagePage.getTotalPages())
                        .last(messagePage.isLast())
                        .build();

                return ResponseEntity.ok(createSuccessResponse(response, "Messages fetched successfully"));
            }

            // ======================================================
            //      IF NO SESSION ID → FETCH SESSIONS
            // ======================================================
            Page<ChatSession> sessionPage;

            if (isAdmin) {
                // 🔥 ADMIN: fetch ALL sessions
                sessionPage = chatSessionRepository.findAll(pageable);
            } else {
                // Normal User: fetch only their own
                sessionPage = chatSessionRepository.findAllByUserId(user.getId(), pageable);
            }

            List<SessionResponse> sessions = sessionPage.getContent().stream()
                    .map(s -> SessionResponse.builder()
                            .id(s.getId())
                            .title(s.getTitle())
                            .createdAt(s.getCreatedAt())
                            .lastMessage(s.getMessages().isEmpty() ? null :
                                    s.getMessages().get(s.getMessages().size() - 1).getContent())
                            .build())
                    .toList();

            PaginationResponse<SessionResponse> response = PaginationResponse.<SessionResponse>builder()
                    .content(sessions)
                    .page(sessionPage.getNumber())
                    .size(sessionPage.getSize())
                    .totalElements(sessionPage.getTotalElements())
                    .totalPages(sessionPage.getTotalPages())
                    .last(sessionPage.isLast())
                    .build();

            return ResponseEntity.ok(createSuccessResponse(response, "Sessions fetched successfully"));

        } catch (Exception ex) {
            log.error("Failed to fetch chats", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(ex.getLocalizedMessage(), "Failed to fetch chats"));
        }
    }


    @Override
    public ResponseEntity<ApiResponse<?>> reviewAIResponse(Principal principal, Long messageId, EditAIResponseRequest request) {
       try {
           Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
           if (optionalUser.isEmpty()) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
           }
           BaseUser admin = optionalUser.get();

           if (!UserRoles.ADMIN.getValue().equalsIgnoreCase(admin.getRole().getName())) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(ResponseUtils.createFailureResponse("access denied", "only admins can access this resource"));
           }




           Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);

           if (messageOpt.isEmpty()) {
               return ResponseEntity.status(HttpStatus.NOT_FOUND)
                       .body(ResponseUtils.createFailureResponse("not found", "Message with id " + messageId + " not found"));

           }

           ChatMessage message = messageOpt.get();

           if (!message.getChatType().toString().equalsIgnoreCase("PROMPT_RESPONSE")) {
               return ResponseEntity.status(HttpStatus.FORBIDDEN)
                       .body(ResponseUtils.createFailureResponse("not found", "Only AI responses can be reviewed or edited"));
           }

           message.setContent(request.getContent());
           message.setStatus(ChatStatus.REVIEWED.name());
           message.setUpdatedAt(LocalDateTime.now());

           chatMessageRepository.save(message);

           return ResponseEntity.status(HttpStatus.OK).body(createSuccessResponse(null,"Message updated successfully"));
       } catch (Exception e) {
           log.warn("login failed");
           return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(createFailureResponse("login failed", "error while reviewing AI respone"));
       }


    }



}
