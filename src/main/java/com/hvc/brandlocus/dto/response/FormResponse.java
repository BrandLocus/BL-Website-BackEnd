package com.hvc.brandlocus.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FormResponse {
    private String totalFormSubmitted;
    private String businessDevelopment;
    private String brandDevelopment;
    private String capacityBuilding;
    private String tradeAndInvestmentFacilitation;
    private String businessQuest;

    private String firstName;
    private String lastName;
    private String email;
    private String serviceNeeded;
    private String industryName;
    private String companyName;
    private String message;
}
