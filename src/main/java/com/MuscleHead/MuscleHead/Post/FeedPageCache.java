package com.MuscleHead.MuscleHead.Post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable cache DTO for feed pages.
 */
record FeedPageCache(
        List<PostResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last,
        int numberOfElements) {

    @JsonCreator
    public FeedPageCache(
            @JsonProperty("content") List<PostResponse> content,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("first") boolean first,
            @JsonProperty("last") boolean last,
            @JsonProperty("numberOfElements") int numberOfElements) {
        this.content = content != null ? content : List.of();
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.number = number;
        this.size = size;
        this.first = first;
        this.last = last;
        this.numberOfElements = numberOfElements;
    }

    static FeedPageCache from(Page<PostResponse> page) {
        return new FeedPageCache(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements());
    }

    Page<PostResponse> toPage(Pageable pageable) {
        return new PageImpl<>(
                content,
                PageRequest.of(number, size, pageable.getSort()),
                totalElements);
    }
}
