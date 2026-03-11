# Routine Package

Workout templates (routines) and exercise instances within them.

## Subpackages

### WorkoutTemplate
- **WorkoutTemplate** – Named routine with a list of exercises.
- **ExerciseInstance** – One exercise in a routine: movement, sets, reps.
- **WorkoutTemplateService** – CRUD for templates.
- **WorkoutTemplateController** – REST API.

### ExerciseInstance
- **ExerciseInstance** – Links a Movement to a routine with prescribed sets/reps.
- Part of a WorkoutTemplate; cascade delete when template is removed.

## How It Works

- A routine is a reusable plan: "Push Day" with Bench, OHP, Tricep Pushdown, etc.
- **SessionLog** can optionally attach to a routine (tracks which routine was followed).
- Movement (from Movement package) is the exercise catalog; ExerciseInstance adds routine-specific params.

## Interactions

- **Workout** – SessionLog references WorkoutTemplate.
- **Movement** – ExerciseInstance references Movement.
