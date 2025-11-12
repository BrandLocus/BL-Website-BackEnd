package com.hvc.brandlocus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserRequest {
    @NotNull(message = "first mame required")
    @NotBlank(message = "first mame required")
    private String firstName;
    @NotNull(message = "last name mame required")
    @NotBlank(message = "last mame required")
    private String lastName;
    @NotNull(message = "email required")
    @NotBlank(message = "email required")
    private String email;
    @NotNull(message = "password required")
    @NotBlank(message = "password required")
    private String password;
    @NotNull(message = "industry name required")
    @NotBlank(message = "industry name  required")
    private String industryName;
    @NotNull(message = "business name required")
    @NotBlank(message = "business name  required")
    private String businessName;

    @NotNull(message = "agreement  required")
    private boolean agreementToReceiveAIGeneratedResponse;
}
