package com.hvc.brandlocus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum UserRoles {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    UserRoles(String value){this.value = value;}

    @JsonCreator
    public static UserRoles fromString(String value) {
        for (UserRoles type : UserRoles.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown User role : " + value);
    }
}
