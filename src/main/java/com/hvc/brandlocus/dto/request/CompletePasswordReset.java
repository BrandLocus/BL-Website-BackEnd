package com.hvc.brandlocus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompletePasswordReset {
    @NotNull(message = "otp required")
    @NotBlank(message = "otp required")
    private String otp;

    @NotNull(message = "password required")
    @NotBlank(message = "password required")
    private String password;

    @NotNull(message = "confirm password required")
    @NotBlank(message = "confirm password required")
    private String confirmPassword;
}
