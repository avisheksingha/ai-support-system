package com.aisupport.orchestration.application.customer.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAssistanceDTO {
    private String title;
    private String summary;
    private List<String> resourceLinks;
    private List<String> suggestedActions;
}
