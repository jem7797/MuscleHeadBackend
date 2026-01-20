# MuscleHead Backend - Architectural Overview

## System-Level Overview

**MuscleHead** is a fitness tracking backend that enables users to:
- Create reusable workout templates (Routines)
- Record completed workout sessions (Workouts)
- Track progression over time
- Manage user profiles and statistics

The system separates **templates** (Routines) from **historical data** (Workouts), allowing users to define exercise plans once and log multiple workout sessions from those plans.

---

## Component Breakdown

### Entities / Models

#### **User**
- **Represents:** User account and profile
- **Owns:**
  - Profile data (username, email, height, weight, privacy settings)
  - Aggregated statistics (lifetime_weight_lifted, highest_weight_lifted, XP)
  - Social metrics (followers, following)
- **Must NOT own:**
  - Direct workout data (accessed via relationship)
  - Direct routine data (accessed via relationship)
- **Relationships:**
  - `@OneToMany` → Workouts (historical records)
  - Referenced by Routines via `user_sub_id` (not a bidirectional relationship)

#### **Routine** (Template)
- **Represents:** Reusable workout template
- **Owns:**
  - Template name
  - List of exercises with order, target reps/sets
  - Creation/update timestamps
- **Must NOT own:**
  - Actual workout performance data
  - Direct Exercise definitions (uses RoutineExercise join entity)
- **Relationships:**
  - `@ManyToOne` → User (owner)
  - `@OneToMany` → RoutineExercise (cascade ALL, orphanRemoval)

#### **Exercise** (Definition)
- **Represents:** Exercise definition catalog (e.g., "Bench Press", "Tricep Extension")
- **Owns:**
  - Exercise name
  - Optional description
- **Must NOT own:**
  - Routine-specific configuration (order, targets)
  - Workout performance data
- **Relationships:**
  - Referenced by RoutineExercise (many-to-many via join entity)

#### **RoutineExercise** (Join Entity)
- **Represents:** Exercise within a Routine with specific configuration
- **Owns:**
  - Order/index within routine
  - Optional target reps
  - Optional target sets
- **Must NOT own:**
  - Exercise definition details (references Exercise)
  - Routine details (references Routine)
- **Relationships:**
  - `@ManyToOne` → Routine
  - `@ManyToOne` → Exercise

#### **Workout** (Historical Record)
- **Represents:** Completed workout session
- **Owns:**
  - Workout date (immutable)
  - Actual performance data (reps, sets, weight, duration)
  - Notes
  - Muscle groups targeted
  - Aggregated metrics (total_weight_lifted, workout_highest_lift)
- **Must NOT own:**
  - Template/routine reference (currently no link to Routine)
  - Exercise definitions (stores workout_name as string)
- **Relationships:**
  - `@ManyToOne` → User (owner)

---

### Repositories

All repositories extend `JpaRepository` and provide standard CRUD + custom query methods.

#### **UserRepository**
- Standard operations: `findById(String subId)`, `findByUsername(String)`
- **Who uses:** UserService

#### **RoutineRepository**
- Standard operations: `findById(Long)`, `findAll()`
- Custom queries: `findByUserSub_id(String)`, `findByUser(User)`
- **Who uses:** RoutineService

#### **ExerciseRepository**
- Standard operations: `findById(Long)`, `save(Exercise)`
- Custom queries: `findByName(String)`
- **Who uses:** RoutineService (for exercise lookup/creation)

#### **RoutineExerciseRepository**
- Standard operations: `findById(Long)`, `save(RoutineExercise)`
- Custom queries: `findByRoutineId(Long)`, `deleteByRoutineId(Long)`
- **Who uses:** RoutineService (indirectly via Routine cascade)

#### **WorkoutRepository**
- Standard operations: `findById(Long)`, `save(Workout)`, `deleteById(Long)`
- Custom queries: `findByUser_SubId(String)` (paginated and non-paginated)
- **Who uses:** WorkoutService

---

### Services

Services contain business logic and are the **only layer with `@Transactional`** annotations.

#### **UserService**
- **Responsibilities:**
  - User CRUD operations
  - Validation (sub_id format, birth year)
  - Duplicate prevention on create
- **Dependencies:** UserRepository
- **Who calls:** UserController
- **Transactions:** All write operations are `@Transactional`

#### **RoutineService**
- **Responsibilities:**
  - Routine CRUD operations
  - Exercise lookup/creation (auto-creates Exercise if not found by name)
  - RoutineExercise relationship management
  - User validation (ensures user exists before creating routine)
- **Dependencies:** RoutineRepository, UserRepository, ExerciseRepository, RoutineExerciseRepository
- **Who calls:** RoutineController
- **Transactions:** All write operations are `@Transactional`
- **Special logic:**
  - On create/update: Validates user exists, resolves Exercise references, sets bidirectional relationships

#### **WorkoutService**
- **Responsibilities:**
  - Workout CRUD operations
  - User statistics updates (highest_weight_lifted)
  - Pagination for workout history
- **Dependencies:** WorkoutRepository, UserRepository
- **Who calls:** WorkoutController
- **Transactions:** All write operations are `@Transactional`
- **Special logic:**
  - After create/update: Updates User.highest_weight_lifted if workout exceeds current max

---

### Controllers

Controllers handle HTTP requests, validation, and response formatting. **No business logic.**

#### **UserController** (`/user/api/`)
- **Endpoints:**
  - `POST /` - Create user (public, no auth required)
  - `GET /?subId=...` or `?username=...` - Get user
  - `PUT /{subId}` - Update user
  - `DELETE /{subId}` - Delete user
- **Dependencies:** UserService
- **Validation:** Uses `@Validated` with `OnCreate`/`OnUpdate` groups

#### **RoutineController** (`/routine/api/`)
- **Endpoints:**
  - `POST /` - Create routine
  - `GET /` - List routines (optional `?subId=...` filter)
  - `GET /{routineId}` - Get routine by ID
  - `PUT /{routineId}` - Update routine
  - `DELETE /{routineId}` - Delete routine
- **Dependencies:** RoutineService
- **Validation:** Uses `@Valid` on request bodies

#### **WorkoutController** (`/workout/api/`)
- **Endpoints:**
  - `POST /` - Create workout
  - `GET /{id}` - Get workout by ID
  - `GET /user/{subId}` - Get paginated workouts for user
  - `PUT /{id}` - Update workout
  - `DELETE /{id}` - Delete workout
- **Dependencies:** WorkoutService
- **Validation:** Uses `@Valid` on request bodies
- **Pagination:** Uses Spring Data `Pageable` for workout history

---

## Interaction Flows

### Request Flow (Standard Pattern)

```
HTTP Request
    ↓
Security Filter (JWT Authentication)
    ↓
Controller
    ├─ Validates request (@Valid)
    ├─ Maps HTTP to domain objects
    └─ Calls Service
         ↓
    Service
    ├─ Business logic
    ├─ Validation
    ├─ @Transactional boundary
    └─ Calls Repository(ies)
         ↓
    Repository
    ├─ JPA query execution
    └─ Returns Entity/Entities
         ↓
    Service
    ├─ Processes results
    └─ Returns Entity/DTO
         ↓
    Controller
    ├─ Maps to HTTP response
    └─ Returns ResponseEntity
         ↓
HTTP Response
```

### Entity Relationship Flow

```
User
  │
  ├─→ (1:N) Workout (historical records)
  │     └─→ Stores: date, performance data, notes
  │
  └─→ (1:N) Routine (templates, via user_sub_id)
        └─→ (1:N) RoutineExercise
              └─→ (N:1) Exercise (definition catalog)
```

**Key Distinction:**
- **Routine** = Template (reusable plan)
- **Workout** = Historical record (what actually happened)
- **Exercise** = Shared catalog (definitions)
- **RoutineExercise** = Template configuration (which exercises, in what order, with what targets)

---

## Templates vs Historical Data

### Templates (Routines)

**Purpose:** Reusable workout plans

**Entities:**
- `Routine` - Template container
- `RoutineExercise` - Exercise configuration within template
- `Exercise` - Shared exercise definitions

**Characteristics:**
- Can be edited/updated
- No date/time of execution
- Contains **targets** (reps, sets) not actuals
- Owned by User
- Used to generate workout sessions (future feature)

**Data Flow:**
```
User creates Routine
  → Defines RoutineExercises (references Exercise catalog)
  → Sets order, target reps/sets
  → Saves as template
```

### Historical Data (Workouts)

**Purpose:** Immutable records of completed sessions

**Entities:**
- `Workout` - Single completed session

**Characteristics:**
- Date is immutable (`@Column(updatable = false)`)
- Contains **actual** performance data
- Aggregates statistics (total_weight_lifted, workout_highest_lift)
- Owned by User
- Used for progression tracking and analytics

**Data Flow:**
```
User completes workout
  → Records actual performance
  → Sets date (immutable)
  → Saves as historical record
  → Updates User statistics (if applicable)
```

**Note:** Currently, Workout and Routine are **not linked**. A future enhancement could add a `routine_id` foreign key to Workout to track which template was used.

---

## Common Data Flows

### 1. Creating a Routine

```
Client Request:
  POST /routine/api/
  Body: {
    "name": "Push Day",
    "user": { "sub_id": "abc123" },
    "routineExercises": [
      {
        "exercise": { "name": "Bench Press" },
        "orderIndex": 0,
        "targetReps": 10,
        "targetSets": 3
      }
    ]
  }

Flow:
  1. RoutineController receives request
  2. RoutineService.createNewRoutine():
     a. Validates user exists (looks up by sub_id)
     b. For each RoutineExercise:
        - If Exercise.id provided → fetch existing Exercise
        - If Exercise.name provided → find by name OR create new Exercise
        - Set RoutineExercise.routine reference
     c. Save Routine (cascade saves RoutineExercises)
  3. Return created Routine with IDs

Response:
  {
    "id": 1,
    "name": "Push Day",
    "routineExercises": [
      {
        "id": 1,
        "exercise": { "id": 5, "name": "Bench Press" },
        "orderIndex": 0,
        "targetReps": 10,
        "targetSets": 3
      }
    ]
  }
```

### 2. Updating a Routine

```
Client Request:
  PUT /routine/api/1
  Body: { "name": "Updated Push Day", "routineExercises": [...] }

Flow:
  1. RoutineService.updateRoutine():
     a. Fetch existing Routine
     b. Update name if provided
     c. If routineExercises provided:
        - Clear existing RoutineExercises list
        - orphanRemoval deletes old RoutineExercises
        - Add new RoutineExercises (resolve Exercises)
        - Set routine references
     d. Save Routine
  2. Return updated Routine
```

### 3. Recording a Completed Workout

```
Client Request:
  POST /workout/api/
  Body: {
    "user": { "sub_id": "abc123" },
    "date": "2024-01-15T10:00:00Z",
    "workout_name": "Push Day",
    "reps": 10,
    "sets": 3,
    "total_weight_lifted": 2250,
    "workout_highest_lift": 250,
    "duration": 60.5
  }

Flow:
  1. WorkoutController receives request
  2. WorkoutService.createNewWorkout():
     a. Validate user exists
     b. Save Workout
     c. updateUserHighestWeightLifted():
        - Compare workout_highest_lift vs User.highest_weight_lifted
        - If workout > user max → update User.highest_weight_lifted
        - Save User
  3. Return created Workout

Response:
  {
    "workout_id": 42,
    "date": "2024-01-15T10:00:00Z",
    "workout_name": "Push Day",
    ...
  }
```

### 4. Fetching Workout History for Progression Graphs

```
Client Request:
  GET /workout/api/user/abc123?page=0&size=20&sort=date,DESC

Flow:
  1. WorkoutController receives request
  2. WorkoutService.getWorkoutsByUserId():
     a. Validate subId
     b. WorkoutRepository.findByUser_SubId(subId, pageable)
        - Executes JPQL: SELECT w FROM Workout w WHERE w.user.sub_id = :subId
        - Returns Page<Workout>
  3. Return paginated results

Response:
  {
    "content": [
      { "workout_id": 42, "date": "2024-01-15", "total_weight_lifted": 2250, ... },
      { "workout_id": 41, "date": "2024-01-13", "total_weight_lifted": 2200, ... }
    ],
    "totalElements": 150,
    "totalPages": 8,
    "page": 0,
    "size": 20
  }
```

**Note:** Currently, generating a workout from a routine is **not implemented**. This would be a future feature that:
- Takes a Routine template
- Creates a new Workout with date = now
- Optionally pre-fills workout_name from routine name
- Links Workout to Routine (would require adding routine_id to Workout)

---

## Read vs Write Paths

### Write Paths (All `@Transactional`)

**Pattern:**
```
Controller → Service (@Transactional) → Repository → Database
                                    ↓
                              Business Logic
                              Validation
                              Side Effects (e.g., update User stats)
```

**Examples:**
- Creating Routine: Validates user, resolves Exercises, sets relationships
- Updating Workout: Updates User.highest_weight_lifted if needed
- Deleting Routine: Cascade deletes RoutineExercises (orphanRemoval)

### Read Paths (No Transactions)

**Pattern:**
```
Controller → Service → Repository → Database
                      ↓
                 Returns Entity/Page
```

**Examples:**
- Getting Routine by ID: Simple repository lookup
- Listing Workouts: Paginated query, no side effects
- Getting User: Direct repository access

---

## Security & Authentication

**Architecture:**
- JWT-based authentication via AWS Cognito
- `JwtAuthenticationFilter` validates tokens on all requests
- `SecurityConfig` defines:
  - Public endpoints: `/`, `/health`, `POST /user/api/` (registration)
  - All other endpoints require authentication
- CORS enabled for all origins

**Flow:**
```
Request → JwtAuthenticationFilter → Validate JWT → Extract sub_id → Continue to Controller
```

---

## Key Architectural Decisions

1. **Templates vs History Separation:**
   - Routines are templates (editable, no dates)
   - Workouts are historical (immutable dates, actual performance)

2. **Exercise Catalog:**
   - Shared Exercise definitions prevent duplication
   - Auto-creation on first use (find by name, create if missing)

3. **Transaction Boundaries:**
   - All transactions at Service layer only
   - Controllers are stateless

4. **JSON Serialization:**
   - `@JsonIgnore` on bidirectional relationship sides to prevent infinite recursion
   - User → Workouts: `@JsonIgnore`
   - Routine → User: `@JsonIgnore`
   - RoutineExercise → Routine: `@JsonIgnore`

5. **Cascading:**
   - Routine → RoutineExercise: `CascadeType.ALL` + `orphanRemoval`
   - Deleting Routine automatically deletes its RoutineExercises

6. **Lazy Loading:**
   - All `@ManyToOne` and `@OneToMany` use `FetchType.LAZY`
   - Prevents N+1 queries, loads relationships on-demand

---

## Future Enhancements (Not Implemented)

1. **Link Workout to Routine:**
   - Add `routine_id` foreign key to Workout
   - Track which template was used for each session

2. **Generate Workout from Routine:**
   - Service method: `createWorkoutFromRoutine(Routine routine)`
   - Pre-fills workout data from template

3. **Progression Tracking:**
   - Aggregate queries for charts
   - Compare targets (Routine) vs actuals (Workout)
