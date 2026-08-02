package com.aisupport.orchestration.infrastructure.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestResponsePage<T> {

    private List<T> content = new ArrayList<>();
    private Integer number = 0;
    private Integer size = 20;
    private Long totalElements = 0L;
    private Integer totalPages = 0;
    private Boolean last = false;
    private Boolean first = true;

    @JsonSetter("page")
    public void setPageMetadata(PageMetadata page) {
        if (page != null) {
            if (page.getNumber() != null) this.number = page.getNumber();
            if (page.getSize() != null) this.size = page.getSize();
            if (page.getTotalElements() != null) this.totalElements = page.getTotalElements();
            if (page.getTotalPages() != null) this.totalPages = page.getTotalPages();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageMetadata {
        private Integer size;
        private Long totalElements;
        private Integer totalPages;
        private Integer number;
    }

    public PageImpl<T> toPage() {
        return new PageImpl<>(
            content == null ? new ArrayList<>() : content,
            PageRequest.of(number == null ? 0 : number, (size == null || size <= 0) ? 20 : size),
            totalElements == null ? 0 : totalElements
        );
    }
}
