package com.MuscleHead.MuscleHead.Notification;

import java.time.Instant;

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
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull(message = "User is required")
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Message is required")
    private String message;

    @Column(name = "medal_id")
    private Long medalId;

    @Column(name = "medal_name")
    private String medalName;

    /** For FOLLOW: the follower's sub_id. For other types: optional actor who triggered the notification. */
    @Column(name = "actor_sub_id")
    private String actorSubId;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
