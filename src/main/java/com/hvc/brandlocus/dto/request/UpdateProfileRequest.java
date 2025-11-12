package com.hvc.brandlocus.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {
    private String profileImageUrl;
    private String firstName;
    private String lastName;
    private String industryName;

    private String businessName;
}
