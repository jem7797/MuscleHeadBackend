# Movement Package

Exercise catalog (movements/exercises).

## What's Here

| File | Purpose |
|------|---------|
| **Movement** | Entity: id, name, areaOfActivation |
| **MovementRepository** | JPA repo |
| **MovementSeeder** | Seeds default exercises on startup (if needed) |

## How It's Used

- **SessionInstance** – References Movement for each logged exercise.
- **ExerciseInstance** – References Movement in workout templates.
- Movements are the shared catalog; users pick from them when logging or building routines.
