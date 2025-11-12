package com.hvc.brandlocus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    BLOCKED("BLOCKED");


    private final String value;

    UserStatus(String value){this.value = value;}

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UserStatus fromString(String value) {
        for (UserStatus type : UserStatus.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown User status : " + value);
    }
}
