package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.PaginationRequest;
import com.hvc.brandlocus.dto.response.AdminChatLogResponse;
import com.hvc.brandlocus.dto.response.ChatMessageResponse;
import com.hvc.brandlocus.dto.response.PaginationResponse;
import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.ChatMessage;
import com.hvc.brandlocus.entities.ChatSession;
import com.hvc.brandlocus.enums.ChatStatus;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.ChatSessionRepository;
import com.hvc.brandlocus.repositories.specification.ChatSessionSpecification;
import com.hvc.brandlocus.services.SessionService;
import com.hvc.brandlocus.utils.ApiResponse;
import com.hvc.brandlocus.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionServiceImpl implements SessionService {
    private final ChatSessionRepository chatSessionRepository;
    private final BaseUserRepository baseUserRepository;

    @Override
    public ResponseEntity<ApiResponse<?>> adminSessionAndChatLogs(Principal principal, Long sessionId, Long userId, String searchTerm, String timeFilter, String startDate, String endDate, PaginationRequest paginationRequest) {
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

            Sort.Direction direction = paginationRequest.getOrder().equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;

            Pageable pageable = PageRequest.of(
                    paginationRequest.getPage(),
                    paginationRequest.getLimit(),
                    Sort.by(direction, paginationRequest.getSortBy())
            );

            if (sessionId != null) {
                // Fetch messages for the specific session
                ChatSession session = chatSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new IllegalArgumentException("Session not found"));
                List<ChatMessage> messages = session.getMessages();

                List<ChatMessageResponse> messageResponses = messages.stream()
                        .map(msg -> ChatMessageResponse.builder()
                                .sessionId(session.getId())
                                .messageId(msg.getId())
                                .userType(msg.getSender().toString())
                                .chatType(msg.getChatType().toString())
                                .content(msg.getContent())
                                .name(msg.getChatSession().getUser().getFirstName() + "" + msg.getChatSession().getUser().getLastName())
                                .industryName(msg.getChatSession().getUser().getIndustryName())
                                .businessName(msg.getChatSession().getUser().getBusinessName())
                                .createdAt(msg.getCreatedAt().toString())
                                .build())
                        .toList();

                return ResponseEntity.ok(createSuccessResponse(messageResponses, "Messages fetched successfully"));
            }
            LocalDate start = null;
            LocalDate end = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (startDate != null && endDate != null) {
                start = LocalDate.parse(startDate, formatter);
                end = LocalDate.parse(endDate, formatter);
            }

            Specification<ChatSession> spec = Specification.allOf(
                    ChatSessionSpecification.byUserId(userId),
                    ChatSessionSpecification.searchTerm(searchTerm),
                    ChatSessionSpecification.byTimeFilter(timeFilter),
                    ChatSessionSpecification.createdBetween(start, end)
            );


            Page<ChatSession> sessionPage = chatSessionRepository.findAll(spec, pageable);

            List<AdminChatLogResponse> sessionResponses = sessionPage.getContent().stream().map(session -> {
                String lastMessage = session.getMessages().isEmpty()
                        ? ""
                        : session.getMessages().getLast().getContent();

                return AdminChatLogResponse.builder()
                        .sessionId(session.getId())
                        .name(session.getTitle())
                        .createdAt(session.getCreatedAt())
                        .status(ChatStatus.ACKNOWLEDGED.toString())
                        .content(lastMessage)
                        .build();
            }).toList();

            PaginationResponse<AdminChatLogResponse> response = PaginationResponse.<AdminChatLogResponse>builder()
                    .content(sessionResponses)
                    .page(sessionPage.getNumber())
                    .size(sessionPage.getSize())
                    .totalElements(sessionPage.getTotalElements())
                    .totalPages(sessionPage.getTotalPages())
                    .last(sessionPage.isLast())
                    .build();

            return ResponseEntity.ok(createSuccessResponse(response, "Sessions fetched successfully"));


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "Failed to fetch chat sessions"));
        }
    }
}
