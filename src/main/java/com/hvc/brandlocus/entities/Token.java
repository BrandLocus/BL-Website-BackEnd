package com.hvc.brandlocus.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;


@Entity
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "token")
public class Token extends BaseEntity{
    private String token;

    private String email;


    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean validated;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isUsed;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean expired;

    private LocalDateTime expiry;

    private LocalDateTime nextRequestAllowedAt;

    public Token() {
        this.validated = false;
        this.expired = false;
        this.expiry = LocalDateTime.now().plus(Duration.ofSeconds(300)); // +5 minutes
        this.token = generateNumericOTP();
        this.isUsed = false;
        this.nextRequestAllowedAt = LocalDateTime.now().plusSeconds(60);
    }

    private String generateNumericOTP() {
        Random random = new Random();
        int otpLength = 6;
        StringBuilder otpBuilder = new StringBuilder();

        for (int i = 0; i < otpLength; i++) {
            int digit = random.nextInt(10);
            otpBuilder.append(digit);
        }

        return otpBuilder.toString();
    }
}
