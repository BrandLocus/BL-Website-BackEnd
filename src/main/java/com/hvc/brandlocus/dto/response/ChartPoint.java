package com.hvc.brandlocus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChartPoint {
    private String label;
    private long value;
    private long totalUsers;          // number of users in this period
    private long totalConversations;
}
