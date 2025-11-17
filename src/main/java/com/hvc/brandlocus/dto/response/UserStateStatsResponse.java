package com.hvc.brandlocus.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserStateStatsResponse {
    private String state;
    private long userCount;
    private double percentage;
}
