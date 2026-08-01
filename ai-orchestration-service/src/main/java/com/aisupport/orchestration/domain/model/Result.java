package com.aisupport.orchestration.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result<T> {
    private boolean success;
    private T data;
    private String errorMessage;
    
    public static <T> Result<T> success(T data) {
        return Result.<T>builder().success(true).data(data).build();
    }
    
    public static <T> Result<T> failure(String errorMessage) {
        return Result.<T>builder().success(false).errorMessage(errorMessage).build();
    }
}
