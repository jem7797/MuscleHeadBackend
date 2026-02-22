package com.MuscleHead.MuscleHead.Notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "notifications")
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipient_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "actor_sub_id", referencedColumnName = "sub_id", nullable = false)
    private User actor;

    @Column(nullable = false)
    private String type;

    private boolean read = false;

    @Column(nullable = false)
    private String createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null || createdAt.isBlank()) {
            createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
