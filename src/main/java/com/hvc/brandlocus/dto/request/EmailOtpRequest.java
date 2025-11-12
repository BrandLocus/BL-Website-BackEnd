package com.hvc.brandlocus.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailOtpRequest {
    private String to;
    private String subject;
    private String body;
    @JsonProperty("isHtml")
    private boolean isHtml;
}
