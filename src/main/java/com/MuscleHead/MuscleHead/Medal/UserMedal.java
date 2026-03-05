package com.MuscleHead.MuscleHead.Medal;

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
@Table(name = "user_medals")
@Data
@NoArgsConstructor
public class UserMedal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "sub_id", nullable = false)
    @NotNull
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "medal_name", nullable = false)
    @NotNull
    private MedalName medalName;

    @Column(name = "awarded_at", nullable = false, updatable = false)
    private Instant awardedAt;

    @PrePersist
    protected void onCreate() {
        if (awardedAt == null) {
            awardedAt = Instant.now();
        }
    }
}
