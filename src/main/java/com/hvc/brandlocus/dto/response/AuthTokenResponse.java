package com.hvc.brandlocus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthTokenResponse {
    private String jwtToken;
    private String refreshToken;
    private Long userId;
    private String role;
    private Boolean isActive;
    private String email;
}
