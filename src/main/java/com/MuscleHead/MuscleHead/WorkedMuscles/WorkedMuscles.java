package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "muscle_group", nullable = false, length = 100)
    private String muscleGroup;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
