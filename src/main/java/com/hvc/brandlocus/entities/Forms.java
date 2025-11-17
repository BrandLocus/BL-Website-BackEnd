package com.hvc.brandlocus.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "forms")
public class Forms extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private BaseUser user;

    private String firstName;

    private String lastName;

    private String email;

    private String serviceNeeded;

    private String industryName;

    private String companyName;

    private String message;

    @Column(nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isActive = true;

}
