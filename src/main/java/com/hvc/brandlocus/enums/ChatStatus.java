package com.hvc.brandlocus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatStatus {
    ACKNOWLEDGED ("ACKNOWLEDGED"),
    REVIEWED("REVIEWED");

    private final String value;

    ChatStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static ChatStatus fromValue(String value) {
        for (ChatStatus type : ChatStatus.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ChatType: " + value);
    }
}
