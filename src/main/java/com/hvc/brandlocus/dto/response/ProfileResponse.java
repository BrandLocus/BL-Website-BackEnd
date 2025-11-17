package com.hvc.brandlocus.dto.response;
import jakarta.persistence.Column;
import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String industryName;
    private String businessName;
    private String country;
    private String state;
    private String role;
    private String profileImageUrl;
}
