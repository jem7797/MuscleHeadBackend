# Workout Package

Workout session logs, session instances (individual exercises in a session), and workout schedules.

## Subpackages

### SessionLog
- **SessionLog** – One workout session: user, date, notes, total weight, duration, optional routine.
- **SessionLogService** – Create, update, delete sessions; sync max lifts.
- **SessionLogController** – REST API for session logs.
- Session can be linked to a **WorkoutTemplate** (routine).

### SessionInstance
- **SessionInstance** – One exercise in a session: movement, sets, reps, weight, duration.
- Lives inside a SessionLog; many instances per session.
- **SessionInstanceService** – CRUD for instances; grouped by session.

### WorkoutSchedule
- **WorkoutSchedule** – User's weekly schedule (e.g. "Monday: Legs", "Wednesday: Upper").
- **WorkoutScheduleService** – Find/update by user.
- **WorkoutScheduleController** – GET/PATCH schedule.

## Data Flow

1. User creates a **SessionLog** (workout session).
2. User adds **SessionInstance** entries (exercises with sets/reps/weight).
3. **MedalService** runs on session create/update for achievements.
4. **WorkedMusclesService** records which muscles were worked.

## Interactions

- **Routine** – SessionLog can reference a WorkoutTemplate.
- **Movement** – SessionInstance references Movement (exercise catalog).
- **Medal** – Medals awarded based on session data.
- **WorkedMuscles** – Per-muscle "last worked" tracking from session instances.
