package com.hvc.brandlocus.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private BaseUser user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;
}
