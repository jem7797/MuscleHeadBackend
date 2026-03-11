# MuscleHead Backend

Spring Boot REST API for the MuscleHead fitness social app. Provides user management, posts, follows, workout tracking, notifications, and live workout sessions.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         MuscleHead Application                           │
├─────────────────────────────────────────────────────────────────────────┤
│  config/          → Security (JWT/Cognito), Redis, CORS                  │
│  cache/           → Redis abstraction for all caching                    │
│  exception/       → Global error handling                                │
│  validation/      → Custom validators (birth year, Cognito sub ID)       │
├─────────────────────────────────────────────────────────────────────────┤
│  User/            → Registration, profiles, search                       │
│  Follow/          → Follow/unfollow, follow requests (private accounts)  │
│  Post/            → Posts, likes, comments, feed                         │
│  s3/              → Presigned URLs for images (posts, profile pics)      │
├─────────────────────────────────────────────────────────────────────────┤
│  Workout/         → Session logs, session instances, schedules           │
│  Routine/         → Workout templates, exercise instances                │
│  Movement/        → Exercise catalog                                    │
│  WorkedMuscles/   → Per-muscle "last worked" tracking                    │
│  Rank/            → User levels/ranks                                   │
├─────────────────────────────────────────────────────────────────────────┤
│  Notification/    → In-app notifications                                │
│  Medal/           → Achievement badges and medal logic                   │
│  LiveSession/     → Live workout sessions with invites                   │
└─────────────────────────────────────────────────────────────────────────┘
```

## Tech Stack

- **Spring Boot 3.5** – Web, Data JPA, Security
- **PostgreSQL** (Supabase) – Primary database
- **Redis** (Upstash) – Caching (users, posts, feed, followers, etc.)
- **AWS Cognito** – JWT authentication
- **AWS S3** – Image storage (presigned URLs; client uploads directly)

## Key Design Decisions

1. **Presigned URLs** – Images never touch the backend; client uploads directly to S3 to avoid proxying large files.
2. **Redis caching** – High-read endpoints (feed, user, followers) use cache-first to reduce DB load.
3. **Stateless auth** – JWT in `Authorization: Bearer`; no server-side sessions.
4. **Privacy settings** – `public` / `private` / `hidden`; private users require follow requests.

## Running Locally

1. Set `.env` with `SUPABASE_DB_USER`, `SUPABASE_DB_PASSWORD`, `UPSTASH_REDIS_REST_TOKEN`, AWS credentials.
2. Use pooler username: `postgres.{projectRef}` for Supabase.
3. `./mvnw spring-boot:run` – app runs on port 8082.

## Package READMEs

Each package has its own `README.md` with details:

| Package | README |
|---------|--------|
| config | [config/README.md](src/main/java/com/MuscleHead/MuscleHead/config/README.md) |
| cache | [cache/README.md](src/main/java/com/MuscleHead/MuscleHead/cache/README.md) |
| User | [User/README.md](src/main/java/com/MuscleHead/MuscleHead/User/README.md) |
| Follow | [Follow/README.md](src/main/java/com/MuscleHead/MuscleHead/Follow/README.md) |
| Post | [Post/README.md](src/main/java/com/MuscleHead/MuscleHead/Post/README.md) |
| s3 | [s3/README.md](src/main/java/com/MuscleHead/MuscleHead/s3/README.md) |
| Workout | [Workout/README.md](src/main/java/com/MuscleHead/MuscleHead/Workout/README.md) |
| Routine | [Routine/README.md](src/main/java/com/MuscleHead/MuscleHead/Routine/README.md) |
| Movement | [Movement/README.md](src/main/java/com/MuscleHead/MuscleHead/Movement/README.md) |
| Rank | [Rank/README.md](src/main/java/com/MuscleHead/MuscleHead/Rank/README.md) |
| Notification | [Notification/README.md](src/main/java/com/MuscleHead/MuscleHead/Notification/README.md) |
| Medal | [Medal/README.md](src/main/java/com/MuscleHead/MuscleHead/Medal/README.md) |
| WorkedMuscles | [WorkedMuscles/README.md](src/main/java/com/MuscleHead/MuscleHead/WorkedMuscles/README.md) |
| LiveSession | [LiveSession/README.md](src/main/java/com/MuscleHead/MuscleHead/LiveSession/README.md) |
| exception | [exception/README.md](src/main/java/com/MuscleHead/MuscleHead/exception/README.md) |
| validation | [validation/README.md](src/main/java/com/MuscleHead/MuscleHead/validation/README.md) |
| resources | [resources/README.md](src/main/resources/README.md) |
| db/migration | [db/migration/README.md](src/main/resources/db/migration/README.md) |
