package com.aisupport.orchestration.domain.context;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationContext {
    private final List<String> eventHistory;
}
