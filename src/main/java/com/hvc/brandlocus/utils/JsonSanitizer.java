package com.hvc.brandlocus.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class JsonSanitizer {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    private static final Set<String> sensitiveFields = Set.of(
            "password", "jwtToken", "token", "accessToken", "refreshToken",
            "apiKey", "x-api-key", "authorization", "secret", "key"
    );

    public static String sanitize(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            Map<String, Object> sanitized = sanitizeMap(map);
            return objectMapper.writeValueAsString(sanitized);
        } catch (Exception e) {
            log.warn("Failed to sanitize JSON: {}", e.getMessage());
            return json; // fallback safely
        }
    }

    private static Map<String, Object> sanitizeMap(Map<String, Object> map) {
        Map<String, Object> sanitized = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (isSensitiveKey(key)) {
                sanitized.put(key, "*****");
            } else if (value instanceof Map<?, ?> nested) {
                sanitized.put(key, sanitizeMap((Map<String, Object>) nested));
            } else if (value instanceof List<?> list) {
                sanitized.put(key, sanitizeList(list));
            } else {
                sanitized.put(key, value);
            }
        }
        return sanitized;
    }

    private static List<Object> sanitizeList(List<?> list) {
        List<Object> sanitized = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> nested) {
                sanitized.add(sanitizeMap((Map<String, Object>) nested));
            } else {
                sanitized.add(item);
            }
        }
        return sanitized;
    }

    private static boolean isSensitiveKey(String key) {
        return sensitiveFields.stream().anyMatch(f -> f.equalsIgnoreCase(key));
    }
}
