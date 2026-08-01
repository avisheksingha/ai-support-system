package com.aisupport.orchestration.infrastructure.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T> {

    private List<T> content = new ArrayList<>();
    private int number;
    private int size = 20;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;

    public PageImpl<T> toPage() {
        return new PageImpl<>(
            content == null ? new ArrayList<>() : content,
            PageRequest.of(number, size <= 0 ? 20 : size),
            totalElements
        );
    }
}
