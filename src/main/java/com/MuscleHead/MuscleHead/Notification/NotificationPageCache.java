package com.MuscleHead.MuscleHead.Notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Serializable cache DTO for notification pages.
 */
record NotificationPageCache(
        List<NotificationResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean first,
        boolean last,
        int numberOfElements) {

    @JsonCreator
    public NotificationPageCache(
            @JsonProperty("content") List<NotificationResponse> content,
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

    static NotificationPageCache from(Page<NotificationResponse> page) {
        return new NotificationPageCache(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.getNumberOfElements());
    }

    Page<NotificationResponse> toPage(Pageable pageable) {
        return new PageImpl<>(
                content,
                PageRequest.of(number, size, pageable.getSort()),
                totalElements);
    }
}
