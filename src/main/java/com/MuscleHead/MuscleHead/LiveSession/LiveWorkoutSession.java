package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "live_workout_sessions")
@Data
@NoArgsConstructor
public class LiveWorkoutSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "host_user_id", nullable = false)
    private String hostUserId;

    @Column(name = "guest_user_id")
    private String guestUserId;

    @Column(name = "host_user_name")
    private String hostUserName;

    @Column(name = "guest_user_name")
    private String guestUserName;

    @Enumerated(EnumType.STRING)
    private SessionStatus status = SessionStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<LiveSessionExercise> exercises;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public enum SessionStatus {
        PENDING,
        in_progress,
        ENDED
    }
}
