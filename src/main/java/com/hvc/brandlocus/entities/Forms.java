package com.hvc.brandlocus.entities;

import com.hvc.brandlocus.enums.FormStatus;
import com.hvc.brandlocus.enums.ServiceNeeded;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forms")
public class Forms extends BaseEntity{

//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private BaseUser user;

    private String firstName;

    private String lastName;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_needed")
    private ServiceNeeded serviceNeeded;

    private String industryName;

    private String companyName;

    private String message;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FormStatus status;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

}
