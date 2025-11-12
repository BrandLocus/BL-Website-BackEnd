package com.hvc.brandlocus.dto.response;

import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private Long userId;
    private String email;
    private Boolean isActive;
    private String role;
    private String jwtToken;
}
