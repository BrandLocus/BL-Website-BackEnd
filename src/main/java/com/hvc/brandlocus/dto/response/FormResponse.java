package com.hvc.brandlocus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hvc.brandlocus.enums.FormStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FormResponse {

    // Statistics fields
    private Long activeConversations;
    private Double activeConversationsChange;
    private Long repliedForms;
    private Double repliedFormsChange;

    // Service breakdown fields
    private Long businessDevelopment;
    private Long brandDevelopment;
    private Long capacityBuilding;
    private Long tradeAndInvestmentFacilitation;
    private Long businessQuest;
    private Long marketingConsulting;
    private Long contact;
    private Long playTest;
    private Long others;



    // Form detail fields
    private Long formId;
    private String firstName;
    private String lastName;
    private String email;
    private String serviceNeeded;
    private String companyName;
    private String industryName;
    private String message;
    private String adminReply;
    private String status;
    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime repliedAt;
}
