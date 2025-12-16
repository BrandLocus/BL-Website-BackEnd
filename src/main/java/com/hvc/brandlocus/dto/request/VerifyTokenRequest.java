package com.hvc.brandlocus.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyTokenRequest {
    private String email;
    private String token;
    private String purpose;
}
