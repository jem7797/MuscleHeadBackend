package com.MuscleHead.MuscleHead.Movement;

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

        List<Movement> list = List.of(
                // Chest
                createMovement("Barbell Bench Press", "Chest"),
                createMovement("Dumbbell Bench Press", "Chest"),
                createMovement("Incline Bench Press", "Chest"),
                createMovement("Decline Bench Press", "Chest"),
                createMovement("Push-Ups", "Chest"),
                createMovement("Cable Chest Fly", "Chest"),
                createMovement("Dumbbell Chest Fly", "Chest"),
                createMovement("Pec Deck Machine", "Chest"),
                createMovement("Chest Press Machine", "Chest"),
                createMovement("Pec Fly Machine", "Chest"),
                
                // Arms
                createMovement("Barbell Bicep Curl", "Arms"),
                createMovement("Dumbbell Bicep Curl", "Arms"),
                createMovement("Hammer Curl", "Arms"),
                createMovement("Preacher Curl", "Arms"),
                createMovement("Concentration Curl", "Arms"),
                createMovement("Cable Curl", "Arms"),
                createMovement("Incline Dumbbell Curl", "Arms"),
                createMovement("EZ Bar Curl", "Arms"),
                createMovement("Overhead Tricep Extension (Dumbbell)", "Arms"),
                createMovement("Overhead Tricep Extension (Cable)", "Arms"),
                createMovement("Skull Crushers", "Arms"),
                createMovement("Tricep Kickbacks", "Arms"),
                createMovement("Close-Grip Bench Press", "Arms"),
                createMovement("Dips", "Arms"),
                createMovement("Machine Tricep Pushdown", "Arms"),
                createMovement("Rope Tricep Pushdown", "Arms"),
                
                // Shoulders
                createMovement("Dumbbell Shoulder Press", "Shoulders"),
                createMovement("Barbell Shoulder Press (Overhead Press)", "Shoulders"),
                createMovement("Arnold Press", "Shoulders"),
                createMovement("Lateral Raise", "Shoulders"),
                createMovement("Front Raise", "Shoulders"),
                createMovement("Rear Delt Fly", "Shoulders"),
                createMovement("Cable Lateral Raise", "Shoulders"),
                createMovement("Cable Front Raise", "Shoulders"),
                createMovement("Cable Rear Delt Fly", "Shoulders"),
                createMovement("Cable Face Pull with Rope", "Shoulders"),
                createMovement("Shoulder Press Machine", "Shoulders"),
                
                // Back
                createMovement("Barbell Row", "Back"),
                createMovement("Dumbbell Row", "Back"),
                createMovement("T-Bar Row", "Back"),
                createMovement("Seated Cable Row", "Back"),
                createMovement("Lat Pulldown", "Back"),
                createMovement("Pull-Ups", "Back"),
                createMovement("Chin-Ups", "Back"),
                createMovement("Inverted Row", "Back"),
                createMovement("Face Pulls", "Back"),
                createMovement("Lat Pullover Machine", "Back"), 
                
                // Legs
                createMovement("Barbell Squat", "Legs"),
                createMovement("Front Squat", "Legs"),
                createMovement("Goblet Squat", "Legs"),
                createMovement("Sumo Squat", "Legs"),
                createMovement("Leg Press Machine", "Legs"),
                createMovement("Lunges (Walking)", "Legs"),
                createMovement("Reverse Lunges", "Legs"),
                createMovement("Step-Ups", "Legs"),
                createMovement("Bulgarian Split Squat", "Legs"),
                createMovement("Leg Extension Machine", "Legs"),
                createMovement("Hamstring Curl Machine", "Legs"),
                createMovement("Romanian Deadlift", "Legs"),
                createMovement("Stiff-Leg Deadlift", "Legs"),
                createMovement("Conventional Deadlift", "Legs"),
                createMovement("Sumo Deadlift", "Legs"),
                createMovement("Jump Squats", "Legs"),
                createMovement("Box Jumps", "Legs"),
                createMovement("Leg Curl (Seated)", "Legs"),
                createMovement("Leg Curl (Lying)", "Legs"),
                
                // Glutes
                createMovement("Hip Thrust", "Glutes"),
                createMovement("Glute Bridge", "Glutes"),
                
                // Calves
                createMovement("Seated Calf Raise", "Calves"),
                createMovement("Standing Calf Raise", "Calves"),
                createMovement("Donkey Calf Raise", "Calves"),
                
                // Abs
                createMovement("Plank", "Abs"),
                createMovement("Side Plank", "Abs"),
                createMovement("Hanging Leg Raise", "Abs"),
                createMovement("Cable Crunch", "Abs"),
                createMovement("Ab Wheel Rollout", "Abs"),
                createMovement("Russian Twist", "Abs"),
                createMovement("Bicycle Crunch", "Abs"),
                createMovement("Mountain Climbers", "Abs"),
                createMovement("Reverse Crunch", "Abs"),
                createMovement("Sit-Ups", "Abs"),
                createMovement("Flutter Kicks", "Abs"),
                createMovement("Back Extension", "Abs"),
                createMovement("Good Mornings", "Abs"),
                
                // Core
                createMovement("Kettlebell Swing", "Core"),
                createMovement("Farmer's Carry", "Core"),
                createMovement("Suitcase Carry", "Core"),
                createMovement("Sled Push", "Core"),
                createMovement("Sled Pull", "Core"),
                createMovement("Rowing Machine", "Core"),
                createMovement("Jump Rope", "Core"),
                createMovement("Running on Treadmill", "Core"),
                createMovement("Incline Treadmill Walk", "Core"),
                createMovement("Stationary Bike", "Core"),
                createMovement("Elliptical", "Core"),
                createMovement("Burpees", "Core"),
                createMovement("Medicine Ball Slam", "Core"),
                
                // Traps
                createMovement("Shrugs", "Traps"),
                createMovement("Upright Row", "Traps")
        );

        movementRepository.saveAll(list);
    }

    private Movement createMovement(String name, String areaOfActivation) {
        Movement movement = new Movement();
        movement.setName(name);
        movement.setAreaOfActivation(areaOfActivation);

        return movement;

    }

}
