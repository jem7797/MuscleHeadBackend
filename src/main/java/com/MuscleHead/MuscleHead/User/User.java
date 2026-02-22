package com.MuscleHead.MuscleHead.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.MuscleHead.MuscleHead.Rank.Rank;
import com.MuscleHead.MuscleHead.Workout.SessionLog.SessionLog;
import com.MuscleHead.MuscleHead.validation.AwsCognitoSubId;
import com.MuscleHead.MuscleHead.validation.OnCreate;
import com.MuscleHead.MuscleHead.validation.ValidBirthYear;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @NotBlank(message = "User ID (sub_id) cannot be blank", groups = OnCreate.class)
    @AwsCognitoSubId(groups = OnCreate.class)
    private String sub_id;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "First name cannot be empty")
    private String first_name;

    @ManyToOne(fetch = jakarta.persistence.FetchType.EAGER)
    @JoinColumn(name = "user_rank_id")
    private Rank rank;

    /** Optional. Use null or 0 when not set. */
    @PositiveOrZero(message = "Height must be 0 or greater")
    private Integer height;

    /** Optional. Use null or 0 when not set. */
    @PositiveOrZero(message = "Weight must be 0 or greater")
    private Integer weight;
    private boolean show_weight = true;
    private boolean show_height = true;
    private boolean stat_tracking = true;
    private String privacy_setting = "public";

    @PositiveOrZero(message = "Lifetime weight lifted cannot be negative")
    private double lifetime_weight_lifted = 0;

    @PositiveOrZero
    private double highest_weight_lifted = 0;

    @PositiveOrZero(message = "Lifetime gym time cannot be negative")
    private double lifetime_gym_time;

    @Min(value = 0, message = "Number of followers cannot be negative")
    private int number_of_followers = 0;

    

    @Min(value = 0, message = "Number following cannot be negative")
    private int number_following = 0;

    private String profilePicUrl;

    @PositiveOrZero(message = "XP cannot be negative")
    private int XP = 0;

    private boolean nattyStatus = true; // Default to natty/yes

    private int number_of_posts = 0;

    @Column(updatable = false)
    @Min(value = 1920, message = "Birth year must be 1920 or later")
    @ValidBirthYear
    @NotNull(message = "Birth year must be provided")
    private Integer birth_year;

    @Column(updatable = false)
    private String date_created;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<SessionLog> workoutSessions;

    /** Optional. Null when not set. */
    private String bio;

    @JsonIgnore
    @OneToMany
    private List<User> nemesis;

@ElementCollection
@CollectionTable(name = "user_workout_schedule", joinColumns = @JoinColumn(name = "user_sub_id"))
@MapKeyColumn(name = "day_of_week")
@Column(name = "workout_label")
private Map<String, String> workoutSchedule = new HashMap<>();


    @PrePersist
    protected void onCreate() {
        if (date_created == null || date_created.isBlank()) {
            date_created = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    
    }

}