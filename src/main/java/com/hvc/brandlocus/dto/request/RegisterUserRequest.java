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
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "industry name required")
    @NotBlank(message = "industry name  required")
    private String industryName;
    @NotNull(message = "business name required")
    @NotBlank(message = "business name  required")
    private String businessName;

    @NotNull(message = "business brief required")
    @NotBlank(message = "business brief  required")
    private String businessBrief;

    @NotNull(message = "country is required")
    @NotBlank(message = "country required")
    private String country;

    @NotNull(message = "state is required")
    @NotBlank(message = "state is required")
    private String state;

    @NotNull(message = "agreement  required")
    private boolean agreementToReceiveAIGeneratedResponse;
}
