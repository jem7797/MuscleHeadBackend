# Worked Muscles API — Backend Reference

Use this document to compare backend implementation with the frontend.

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/workedMuscles/api/` | Upsert worked muscles from exercises |
| POST | `/api/worked-muscles` | Same as above (alternate path) |
| GET | `/workedMuscles/api/{userId}` | Get worked muscles for user |
| GET | `/api/worked-muscles/{userId}` | Same as above (alternate path) |

**Base URL:** `http://localhost:8082` (or your server URL)

**Auth:** All endpoints require JWT. POST requires `userId` to match the authenticated user's `sub_id`.

---

## POST — Request

**URL:** `POST /workedMuscles/api/` or `POST /api/worked-muscles`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer <jwt>`

**Body:**
```json
{
  "userId": "cognito-sub-id-string",
  "exercises": [
    {
      "exerciseId": 1,
      "sets": 3,
      "reps": 10,
      "weight": 135
    }
  ]
}
```

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| userId | string | Yes | Cognito `sub_id` (e.g. `94986468-a081-70d5-d999-ef5244a5d74c`) |
| exercises | array | Yes | Same structure as `SessionLogRequest.exercises` |
| exercises[].exerciseId | number | Yes | Movement ID from `exercises` table (same as session log) |
| exercises[].sets | number | No | Optional |
| exercises[].reps | number | No | Optional |
| exercises[].weight | number | No | Optional |

**Response:** `204 No Content` (empty body)

---

## GET — Response

**URL:** `GET /workedMuscles/api/{userId}` or `GET /api/worked-muscles/{userId}`

**Response:** `200 OK`
```json
{
  "frontWorked": ["chest", "biceps", "triceps", "delts"],
  "backWorked": ["triceps", "delts"]
}
```

| Field | Type | Notes |
|-------|------|-------|
| frontWorked | string[] | Muscle IDs for front-view SVG (MuscleManFront) |
| backWorked | string[] | Muscle IDs for back-view SVG (MuscleManBack) |

**Empty state:** `{ "frontWorked": [], "backWorked": [] }`

---

## Naming Conventions

| Concept | Backend Name | Notes |
|---------|--------------|-------|
| User ID | `userId`, `sub_id` | Cognito subject ID |
| Exercise ID | `exerciseId` | Same as Movement ID in `exercises` table |
| Response fields | `frontWorked`, `backWorked` | camelCase (JSON) |

---

## Muscle ID Mapping (Backend Output)

Fine-grained mapping: Triceps, Biceps, Forearms are stored separately (no Arms).

### Front view (`frontWorked`)

| Canonical (DB) | Muscle IDs Returned |
|----------------|---------------------|
| Chest | `pecs` |
| Triceps | `triceps` |
| Biceps | `biceps` |
| Forearms | `forearms` |
| Shoulders | `delts` |
| Legs | `quads` |
| Calves | `calves` |
| Abs | `abs` |
| Core | `obliques` |
| Back | *(none)* |
| Glutes | *(none)* |
| Traps | *(none)* |

### Back view (`backWorked`)

| Canonical (DB) | Muscle IDs Returned |
|----------------|---------------------|
| Triceps | `triceps` |
| Forearms | `forearms` |
| Shoulders | `delts` |
| Back | `lats` |
| Legs | `hamstrings` |
| Glutes | `glutes` |
| Calves | `calves` |
| Core | `obliques` |
| Traps | `traps` |
| Chest | *(none)* |
| Biceps | *(none)* |
| Abs | *(none)* |

---

## areaOfActivation → Canonical Mapping (Input)

Movement `areaOfActivation` is a comma-separated string (e.g. `"Chest, Triceps, Delts"`). Tokens are lowercased and mapped directly (no collapsing):

| Raw Token (from Movement) | Canonical (stored in DB) |
|---------------------------|--------------------------|
| chest | Chest |
| triceps | Triceps |
| biceps | Biceps |
| forearms | Forearms |
| delts | Shoulders |
| lats | Back |
| quads, hamstrings | Legs |
| glutes | Glutes |
| calves | Calves |
| abs | Abs |
| obliques | Core |
| traps | Traps |

---

## Data Flow

1. **Movement (exercises table):** `id`, `name`, `area_of_activation` (e.g. `"Chest, Triceps, Delts"`)
2. **POST:** Frontend sends `exerciseId` (Movement.id) + exercises array
3. **Backend:** Looks up Movement by `exerciseId` → parses `areaOfActivation` → maps to canonical → stores in `worked_muscles`
4. **GET:** Reads `worked_muscles` → maps canonical to muscle IDs → returns `frontWorked`, `backWorked`

---

## Database

**Table:** `worked_muscles`

| Column | Type | Notes |
|--------|------|-------|
| user_id | VARCHAR(255) | PK, FK to users(sub_id) |
| muscle_groups | TEXT[] | Canonical names: Chest, Arms, Shoulders, etc. |
| updated_at | TIMESTAMP | Last update |

---

## Cache

- **Key:** `workedMuscles:v2:{userId}` (v2 = fine-grained mapping, busts stale Arms data)
- **TTL:** 5 minutes (300 seconds)
- **Bust:** On POST for that user

---

## Example: Barbell Bench Press

- **Movement:** `areaOfActivation = "Chest, Triceps, Delts"`
- **Canonical extracted:** Chest, Triceps, Shoulders (no Arms — triceps maps to Triceps directly)
- **GET response:** `frontWorked: ["pecs", "triceps", "delts"]`, `backWorked: ["triceps", "delts"]`
- **Biceps not included** — only muscles actually trained
