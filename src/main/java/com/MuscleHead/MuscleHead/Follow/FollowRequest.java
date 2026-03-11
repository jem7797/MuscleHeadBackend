package com.MuscleHead.MuscleHead.Follow;

import java.time.Instant;
import java.util.UUID;

import com.MuscleHead.MuscleHead.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follow_requests")
@Data
@NoArgsConstructor
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_sub_id", referencedColumnName = "sub_id", nullable = false)
    @JsonIgnore
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "followee_sub_id", referencedColumnName = "sub_id", nullable = false)
    @JsonIgnore
    private User followee;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.pending;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public enum RequestStatus {
        pending,
        accepted,
        declined
    }
}
