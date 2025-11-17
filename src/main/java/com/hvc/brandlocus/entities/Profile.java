package com.hvc.brandlocus.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile")
public class Profile extends BaseEntity{
    @OneToOne
    @MapsId
    @JoinColumn(name = "id",nullable = false)
    private BaseUser user;
    @Column(columnDefinition = "varchar",nullable = false)
    private String firstName;
    @Column(columnDefinition = "varchar",nullable = false)
    private String lastName;
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String industryName;

    @Column(nullable = false)
    private String businessName;

    private String role;

    private String country;

    private String state;

    private String profileImageUrl;





}
