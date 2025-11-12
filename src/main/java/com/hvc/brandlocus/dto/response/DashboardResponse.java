package com.hvc.brandlocus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {
    private long totalConversations;
    private long activeUsers;
    private Double conversationChange; // null for alltime
    private Double userChange;
    private List<ChartPoint> chartData;
}
