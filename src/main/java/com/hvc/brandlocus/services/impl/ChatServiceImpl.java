package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.ChatMessageRequest;
import com.hvc.brandlocus.dto.request.EditAIResponseRequest;
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

            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        createFailureResponse("User does not exist",
                                "User with email " + principal.getName() + " not found")
                );
            }
            BaseUser user = optionalUser.get();

            ChatSession session;
            if (request.getSessionId() == null) {
                ChatSession newSession = ChatSession.builder()
                        .user(user)
                        .title(request.getTitle() != null ? request.getTitle() : "New Chat")
                        .build();
                session = chatSessionRepository.save(newSession);
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


            String aiAnswer = openAIService.getResponseWithHistory(chatHistory, request.getContent());

            // Save AI message
            ChatMessage aiMessage = chatMessageRepository.save(
                    ChatMessage.builder()
                            .chatSession(session)
                            .sender(SenderType.AI)
                            .chatType(ChatType.PROMPT_RESPONSE)
                            .content(aiAnswer)
                            .build()
            );

            log.info("User message saved, session ID: {}", session.getId());
            log.info("Chat history size: {}", chatHistory.size());


            ChatMessageResponse userMessageResponse = ChatMessageResponse.builder()
                    .sessionId(session.getId())
                    .messageId(userMessage.getId())
                    .userType(userMessage.getSender().toString())
                    .chatType(userMessage.getChatType().toString())
                    .content(userMessage.getContent())
                    .createdAt(userMessage.getCreatedAt().toString())
                    .build();

            ChatMessageResponse aiMessageResponse = ChatMessageResponse.builder()
                    .sessionId(session.getId())
                    .messageId(aiMessage.getId())
                    .userType(aiMessage.getSender().toString())
                    .chatType(aiMessage.getChatType().toString())
                    .content(aiMessage.getContent())
                    .createdAt(aiMessage.getCreatedAt().toString())
                    .build();

            return ResponseEntity.ok(
                    createSuccessResponse(
                            List.of(userMessageResponse, aiMessageResponse),
                            "Message processed successfully"
                    )
            );

        }catch (Exception ex) {
            log.error("Failed to process chat", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    createFailureResponse(ex.getLocalizedMessage(), "Failed to process chat")
            );
        }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> getChats(
            Principal principal,
            Long sessionId,
            PaginationRequest paginationRequest
    ) {
        try {
            log.info("session id :{}",sessionId);
            Optional<BaseUser> optionalUser = baseUserRepository.findByEmail(principal.getName().trim());
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("User not found", "User with email " + principal.getName() + " does not exist"));
            }
            BaseUser user = optionalUser.get();

            Sort.Direction direction = paginationRequest.getOrder().equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(paginationRequest.getPage(),
                    paginationRequest.getLimit(),
                    Sort.by(direction, paginationRequest.getSortBy()));

            if (sessionId != null) {

                ChatSession session = chatSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));

                if (!session.getUser().getId().equals(user.getId())) {
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

            } else {
                Page<ChatSession> sessionPage = chatSessionRepository.findAllByUserId(user.getId(), pageable);

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

                return ResponseEntity.status(HttpStatus.OK).body(createSuccessResponse(response,"Sessions fetched successfully"));


            }

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
