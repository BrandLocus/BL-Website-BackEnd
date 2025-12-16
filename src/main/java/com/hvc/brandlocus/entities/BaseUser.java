package com.hvc.brandlocus.entities;


import com.hvc.brandlocus.utils.AttributeEncryptor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {@Index(name = "idx_users", columnList = "email")})
public class BaseUser extends BaseEntity implements UserDetails {

    @Column(nullable = false)
    private String firstName;

    @Column( nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column( nullable = false)
    @Convert(converter = AttributeEncryptor.class)
    private String password;

    @Column(nullable = false)
    private String industryName;


    @Column(nullable = false)
    private String businessName;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String businessBrief;


    private String profileImageUrl;


    @ManyToOne
    private Roles role;

    private String country;

    private String state;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;


    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActive = false;

    @Column(nullable = false)
    private String userStatus;

    @Column(nullable = false)
    private int chatRequestCount = 0;

    @Version
    private Long version;


    private Boolean agreementToReceiveAIGeneratedResponse = false;

    private LocalDateTime agreementToReceiveAIGeneratedResponseTimestamp;


    @Override
    public String getUsername() {
        return email ;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.getName() == null) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.getName()));
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // BLOCKED users are disabled
        return true;
    }

}
