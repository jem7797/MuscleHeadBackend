package com.MuscleHead.MuscleHead.User;

import java.util.List;

import com.MuscleHead.MuscleHead.Workout.Workout;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @NotBlank(message = "User ID (sub_id) cannot be blank")
    private String sub_id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @Positive(message = "Height must be a positive number")
    private int height;

    @Positive(message = "Weight must be a positive number")
    private int weight;
    private boolean show_weight;
    private boolean show_height;
    private boolean stat_tracking;
    private String privacy_setting;

    @PositiveOrZero(message = "Lifetime weight lifted cannot be negative")
    private double lifetime_weight_lifted;

    @PositiveOrZero(message = "Lifetime gym time cannot be negative")
    private double lifetime_gym_time;

    @Min(value = 0, message = "Number of followers cannot be negative")
    private int number_of_followers;

    @Min(value = 0, message = "Number following cannot be negative")
    private int number_following;

    private String profilePicUrl;

    @PositiveOrZero(message = "XP cannot be negative")
    private double XP;
    private boolean nattyStatus;

    @Column(updatable = false)
    @Min(value = 1920, message = "Birth year must be 1900 or later")
    @Max(value = 2010, message = "Birth year must be 2010 or earlier")
    @NotBlank
    private int birth_year;

    @Column(updatable = false)
    @NotBlank
    private String date_created;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Workout> workouts;

}