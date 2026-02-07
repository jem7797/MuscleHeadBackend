package com.MuscleHead.MuscleHead.Movement;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MovementSeeder implements CommandLineRunner {

    private final MovementRepository movementRepository;

    public MovementSeeder(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Override
    public void run(String... args) {
        seedMovements();
    }

    public void seedMovements() {
        if (movementRepository.count() > 0) {
            return;
        }

        List<Movement> list = new ArrayList<>();
        // Chest
        list.add(createMovement("Barbell Bench Press", "Chest, Triceps, Delts"));
        list.add(createMovement("Dumbbell Bench Press", "Chest, Triceps, Delts"));
        list.add(createMovement("Incline Bench Press", "Chest, Triceps, Delts"));
        list.add(createMovement("Decline Bench Press", "Chest, Triceps, Delts"));
        list.add(createMovement("Push-Ups", "Chest, Triceps, Delts, Abs"));
        list.add(createMovement("Cable Chest Fly", "Chest"));
        list.add(createMovement("Dumbbell Chest Fly", "Chest"));
        list.add(createMovement("Pec Deck Machine", "Chest"));
        list.add(createMovement("Chest Press Machine", "Chest"));
        list.add(createMovement("Pec Fly Machine", "Chest"));
        list.add(createMovement("Lat Pullover Machine", "Chest, Lats"));
        // Biceps
        list.add(createMovement("Barbell Bicep Curl", "Biceps, Forearms"));
        list.add(createMovement("Dumbbell Bicep Curl", "Biceps, Forearms"));
        list.add(createMovement("Hammer Curl", "Biceps, Forearms"));
        list.add(createMovement("Preacher Curl", "Biceps, Forearms"));
        list.add(createMovement("Concentration Curl", "Biceps"));
        list.add(createMovement("Cable Curl", "Biceps, Forearms"));
        list.add(createMovement("Incline Dumbbell Curl", "Biceps"));
        list.add(createMovement("EZ Bar Curl", "Biceps, Forearms"));
        // Triceps
        list.add(createMovement("Overhead Tricep Extension (Dumbbell)", "Triceps"));
        list.add(createMovement("Overhead Tricep Extension (Cable)", "Triceps"));
        list.add(createMovement("Skull Crushers", "Triceps, Chest"));
        list.add(createMovement("Tricep Kickbacks", "Triceps"));
        list.add(createMovement("Close-Grip Bench Press", "Triceps, Chest"));
        list.add(createMovement("Dips", "Triceps, Chest, Delts"));
        list.add(createMovement("Machine Tricep Pushdown", "Triceps"));
        list.add(createMovement("Rope Tricep Pushdown", "Triceps"));
        // Shoulders (Delts)
        list.add(createMovement("Dumbbell Shoulder Press", "Delts, Triceps"));
        list.add(createMovement("Barbell Shoulder Press (Overhead Press)", "Delts, Triceps"));
        list.add(createMovement("Arnold Press", "Delts, Triceps"));
        list.add(createMovement("Lateral Raise", "Delts"));
        list.add(createMovement("Front Raise", "Delts"));
        list.add(createMovement("Rear Delt Fly", "Delts"));
        list.add(createMovement("Cable Lateral Raise", "Delts"));
        list.add(createMovement("Cable Front Raise", "Delts"));
        list.add(createMovement("Cable Rear Delt Fly", "Delts"));
        list.add(createMovement("Cable Face Pull with Rope", "Delts, Traps"));
        list.add(createMovement("Shoulder Press Machine", "Delts, Triceps"));
        // Lats
        list.add(createMovement("Barbell Row", "Lats, Traps, Biceps"));
        list.add(createMovement("Dumbbell Row", "Lats, Traps, Biceps"));
        list.add(createMovement("T-Bar Row", "Lats, Traps, Biceps"));
        list.add(createMovement("Seated Cable Row", "Lats, Traps, Biceps"));
        list.add(createMovement("Lat Pulldown", "Lats, Biceps"));
        list.add(createMovement("Pull-Ups", "Lats, Biceps, Delts"));
        list.add(createMovement("Chin-Ups", "Lats, Biceps"));
        list.add(createMovement("Inverted Row", "Lats, Traps, Biceps"));
        list.add(createMovement("Face Pulls", "Delts, Traps"));
        list.add(createMovement("Lat Pullover Machine", "Lats, Chest"));
        // Traps
        list.add(createMovement("Shrugs", "Traps"));
        list.add(createMovement("Upright Row", "Delts, Traps"));
        // Quads
        list.add(createMovement("Barbell Squat", "Quads, Glutes, Hamstrings"));
        list.add(createMovement("Front Squat", "Quads, Glutes"));
        list.add(createMovement("Goblet Squat", "Quads, Glutes"));
        list.add(createMovement("Sumo Squat", "Quads, Glutes, Hamstrings"));
        list.add(createMovement("Leg Press Machine", "Quads, Glutes"));
        list.add(createMovement("Lunges (Walking)", "Quads, Glutes"));
        list.add(createMovement("Reverse Lunges", "Quads, Hamstrings, Glutes"));
        list.add(createMovement("Step-Ups", "Quads, Glutes"));
        list.add(createMovement("Bulgarian Split Squat", "Quads, Glutes"));
        list.add(createMovement("Leg Extension Machine", "Quads"));
        list.add(createMovement("Jump Squats", "Quads, Glutes"));
        list.add(createMovement("Box Jumps", "Quads, Calves, Glutes"));
        list.add(createMovement("Sumo Deadlift", "Quads, Glutes, Hamstrings"));
        // Hamstrings
        list.add(createMovement("Hamstring Curl Machine", "Hamstrings"));
        list.add(createMovement("Romanian Deadlift", "Hamstrings, Glutes"));
        list.add(createMovement("Stiff-Leg Deadlift", "Hamstrings, Glutes"));
        list.add(createMovement("Conventional Deadlift", "Hamstrings, Glutes, Quads, Traps"));
        list.add(createMovement("Leg Curl (Seated)", "Hamstrings"));
        list.add(createMovement("Leg Curl (Lying)", "Hamstrings"));
        list.add(createMovement("Good Mornings", "Hamstrings, Glutes"));
        list.add(createMovement("Back Extension", "Hamstrings, Glutes"));
        // Glutes
        list.add(createMovement("Hip Thrust", "Glutes, Hamstrings"));
        list.add(createMovement("Glute Bridge", "Glutes, Hamstrings"));
        // Calves
        list.add(createMovement("Seated Calf Raise", "Calves"));
        list.add(createMovement("Standing Calf Raise", "Calves"));
        list.add(createMovement("Donkey Calf Raise", "Calves"));
        // Abs
        list.add(createMovement("Plank", "Abs"));
        list.add(createMovement("Hanging Leg Raise", "Abs"));
        list.add(createMovement("Cable Crunch", "Abs"));
        list.add(createMovement("Ab Wheel Rollout", "Abs"));
        list.add(createMovement("Reverse Crunch", "Abs"));
        list.add(createMovement("Sit-Ups", "Abs"));
        list.add(createMovement("Flutter Kicks", "Abs"));
        // Obliques
        list.add(createMovement("Side Plank", "Obliques"));
        list.add(createMovement("Russian Twist", "Obliques, Abs"));
        list.add(createMovement("Bicycle Crunch", "Abs, Obliques"));
        // Abs + Obliques / multi
        list.add(createMovement("Mountain Climbers", "Abs, Quads, Delts"));
        // Core / full body
        list.add(createMovement("Kettlebell Swing", "Glutes, Hamstrings, Delts"));
        list.add(createMovement("Farmer's Carry", "Traps, Forearms"));
        list.add(createMovement("Suitcase Carry", "Obliques, Forearms"));
        list.add(createMovement("Sled Push", "Quads, Glutes, Calves"));
        list.add(createMovement("Sled Pull", "Hamstrings, Glutes"));
        list.add(createMovement("Rowing Machine", "Lats, Biceps, Delts"));
        list.add(createMovement("Jump Rope", "Calves, Delts"));
        list.add(createMovement("Running on Treadmill", "Quads, Hamstrings, Calves"));
        list.add(createMovement("Incline Treadmill Walk", "Quads, Calves"));
        list.add(createMovement("Stationary Bike", "Quads"));
        list.add(createMovement("Elliptical", "Quads, Glutes, Calves"));
        list.add(createMovement("Burpees", "Chest, Delts, Triceps, Quads, Abs"));
        list.add(createMovement("Medicine Ball Slam", "Abs, Delts"));

        movementRepository.saveAll(list);
    }

    private Movement createMovement(String name, String areaOfActivation) {
        Movement m = new Movement();
        m.setName(name);
        m.setAreaOfActivation(areaOfActivation);
        return m;
    }
}
