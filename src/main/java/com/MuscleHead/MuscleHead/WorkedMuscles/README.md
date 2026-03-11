# WorkedMuscles Package

Tracks which muscles were last worked and when (for "body map" / recovery UI).

## What's Here

| File | Purpose |
|------|---------|
| **WorkedMuscles** | Entity: userId, muscle group, expiresAt |
| **WorkedMusclesRepository** | Find by user and expiry |
| **WorkedMusclesService** | Compute worked muscles from session instances; cache |
| **WorkedMusclesResponse** | DTO with front/back view muscle data |
| **WorkedMusclesCleanupScheduler** | Cron job to delete expired rows |

## How It Works

1. **SessionInstance** has `areaOfActivation` (e.g. "chest", "triceps").
2. **WorkedMusclesService** maps these to canonical muscle groups (Chest, Triceps, etc.).
3. Rows are stored with `expiresAt` (default 48 hours).
4. **WorkedMusclesCleanupScheduler** runs daily to remove expired rows.

## Caching

- Per-user response cached 5 min.
- Invalidated when user logs a workout.

## Muscle Mapping

- Raw tokens (from Movement.areaOfActivation) → canonical names (Chest, Triceps, etc.).
- Canonical → front/back view IDs for the body map UI.
