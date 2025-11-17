package com.hvc.brandlocus.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFormRequest {
    private String firstName;

    private String lastName;

    private String email;

    private String serviceNeeded;

    private String industryName;

    private String companyName;

    private String message;
}
