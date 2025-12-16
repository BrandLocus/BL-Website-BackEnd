package com.hvc.brandlocus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @NotNull(message = "All fields required")
    @NotBlank(message = "All fields required")
    private String password;

}
