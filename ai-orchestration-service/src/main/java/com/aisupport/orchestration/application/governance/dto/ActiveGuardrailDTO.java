package com.aisupport.orchestration.application.governance.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActiveGuardrailDTO {
    private String name;
    private String type;
    private String status;
    private Integer count;
}
