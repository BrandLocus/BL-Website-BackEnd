package com.hvc.brandlocus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private String firstName;
    private String lastName;
    private Long sessionId;
    private Long messageId;
    private String userType;
    private String chatType;
    private String content;
    private String name;
    private String businessName;
    private String businessBrief;
    private String state;
    private String country;
    private String sector;
    private String keywords;
    private String topic;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdAt;
}
