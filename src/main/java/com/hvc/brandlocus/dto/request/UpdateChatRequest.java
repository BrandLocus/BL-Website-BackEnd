package com.hvc.brandlocus.dto.request;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateChatRequest {
    private String content;
}
