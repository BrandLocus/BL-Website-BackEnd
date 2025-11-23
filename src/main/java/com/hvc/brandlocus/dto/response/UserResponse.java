package com.hvc.brandlocus.dto.response;

import com.hvc.brandlocus.entities.Roles;
import com.hvc.brandlocus.utils.AttributeEncryptor;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private  Long userId;

    private String firstName;

    private String lastName;

    private String email;

    private String industryName;


    private String businessName;


    private String profileImageUrl;

    private String role;

    private String state;
    private String country;
}
