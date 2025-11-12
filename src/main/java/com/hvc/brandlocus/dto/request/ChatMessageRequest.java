package com.hvc.brandlocus.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    private Long sessionId;
    private String content;
    private String title;
}
