package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.LocalDateTime;

import com.MuscleHead.MuscleHead.User.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "worked_muscles", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(columnNames = {"user_id", "muscle_group"})
})
@Data
@NoArgsConstructor
public class WorkedMuscles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "muscle_group", nullable = false, length = 100)
    private String muscleGroup;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Convenience accessor for the owning user's ID.
     * Delegates to the mapped User relationship.
     */
    public String getUserId() {
        return user != null ? user.getSub_id() : null;
    }
}
