package com.MuscleHead.MuscleHead.Rank;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rank")
@Data
@NoArgsConstructor
public class Rank {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Level is required")
    @PositiveOrZero(message = "Level must be 0 or greater")
    private Integer level;

    @NotBlank(message = "Rank name cannot be blank")
    private String name;
}
