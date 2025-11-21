package com.MuscleHead.MuscleHead.Workout;

import java.time.Instant;
import java.util.List;

import com.MuscleHead.MuscleHead.User.User;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "workouts")

public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long workout_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_id", referencedColumnName = "sub_id", nullable = false)
    private User user;

    @Column(updatable = false, columnDefinition = "TIMESTAMP")
    private Instant date;
    
    private String notes;
    private String workout_name;

    @ElementCollection
    private List<String> area_of_activation;

    private int reps;
    private int sets;
    private double duration;
    private double total_weight_lifted;

    public long getWorkout_id() {
        return workout_id;
    }

    public void setWorkout_id(int workout_id) {
        this.workout_id = workout_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWorkout_name() {
        return workout_name;
    }

    public void setWorkout_name(String workout_name) {
        this.workout_name = workout_name;
    }

    public List<String> getArea_of_activation() {
        return area_of_activation;
    }

    public void setArea_of_activation(List<String> area_of_activation) {
        this.area_of_activation = area_of_activation;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getTotal_weight_lifted() {
        return total_weight_lifted;
    }

    public void setTotal_weight_lifted(double total_weight_lifted) {
        this.total_weight_lifted = total_weight_lifted;
    }

}
