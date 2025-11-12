package com.hvc.brandlocus.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationRequest {
    private int page;
    private int limit;
    private String sortBy;
    private String order;

}
