package com.hvc.brandlocus.enums;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ServiceNeeded {
    BUSINESS_DEVELOPMENT("Business Development"),
    BRAND_DEVELOPMENT("Brand Development"),
    CAPACITY_BUILDING("Capacity Building"),
    TRADE_AND_INVESTMENT_FACILITATION("Trade & Investment Facilitation"),
    BUSINESS_QUEST("Business Quest"),
    MARKETING_CONSULTING("Marketing Consulting"),
    CONTACT("Contact"),
    PLAY_TEST("PlayTest"),
    OTHERS("OTHERS");

    private final String value;

    ServiceNeeded(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ServiceNeeded fromString(String value) {
        for (ServiceNeeded s : ServiceNeeded.values()) {

            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }

            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown Service Needed: " + value);
    }

}

