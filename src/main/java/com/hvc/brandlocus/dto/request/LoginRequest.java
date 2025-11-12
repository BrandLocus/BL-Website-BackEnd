package com.hvc.brandlocus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotNull(message = "phone number required")
    @NotBlank(message = "phone number required")
    private String email;
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$", message = "Password must be at least 8 characters long and include alphanumeric characters and special characters")
    @NotNull(message = "All fields required")
    @NotBlank(message = "All fields required")
    private String password;
}
