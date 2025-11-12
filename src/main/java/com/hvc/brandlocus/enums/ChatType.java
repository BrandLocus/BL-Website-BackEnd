package com.hvc.brandlocus.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatType {
    PROMPT("PROMPT"),
    PROMPT_RESPONSE("PROMPT_RESPONSE");

    private final String value;

    ChatType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static ChatType fromValue(String value) {
        for (ChatType type : ChatType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ChatType: " + value);
    }
}
