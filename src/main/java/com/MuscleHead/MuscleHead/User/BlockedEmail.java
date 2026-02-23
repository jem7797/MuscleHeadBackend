package com.MuscleHead.MuscleHead.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Emails blocked from signup because they were used by someone under 13.
 * Block is lifted when that person turns 13 (birth_date + 13 years).
 */
@Entity
@Table(name = "blocked_emails")
@Data
@NoArgsConstructor
public class BlockedEmail {

    @Id
    private String email;

    @Column(nullable = false)
    private LocalDate birth_date;

    @Column(nullable = false)
    private String created_at;

    @PrePersist
    protected void onCreate() {
        if (created_at == null || created_at.isBlank()) {
            created_at = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    /** True if this person has turned 13 (block can be lifted). */
    public boolean hasTurned13() {
        return java.time.Period.between(birth_date, LocalDate.now()).getYears() >= 13;
    }
}
