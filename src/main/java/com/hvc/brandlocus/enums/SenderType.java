package com.hvc.brandlocus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SenderType {
    USER("USER"),
    AI("AI");

    private final String value;

    SenderType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static SenderType fromValue(String value) {
        for (SenderType type : SenderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown senderType: " + value);
    }

    public static SenderType fromString(String value) {
        for (SenderType type : SenderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown senderType: " + value);
    }
}
