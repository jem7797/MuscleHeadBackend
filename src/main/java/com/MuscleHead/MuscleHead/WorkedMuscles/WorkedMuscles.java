package com.MuscleHead.MuscleHead.WorkedMuscles;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.MuscleHead.MuscleHead.User.User;

@Entity
@Table(name = "worked_muscles")
@Data
@NoArgsConstructor
public class WorkedMuscles {

    @Id
    @Column(name = "user_id")
    private String userId;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "muscle_groups")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> muscleGroups;

    @Column(name = "updated_at")
    private Instant updatedAt;
}
