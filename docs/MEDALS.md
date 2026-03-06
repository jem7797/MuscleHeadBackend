# MuscleHead Medals System

This document describes the achievements (medals) system in the MuscleHead backend, including how each medal is earned and where the logic is triggered.

---

## Overview

Medals are awarded to users when they meet specific criteria. Each medal is stored in the `user_medals` table and triggers a `MEDAL_EARNED` notification. Medals are only awarded once per user (duplicates are prevented by `UserMedalRepository.existsByUserSubIdAndMedalName`).

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/medal/api/` | Returns only medals the user has earned, ordered by awardedAt descending |
| `GET` | `/medal/api/all` | Returns the full catalog of all medals with `earned` status for the authenticated user |

**GET /medal/api/ response (earned only):**
```json
[
  {
    "id": 1,
    "medalName": "BAPTISM",
    "description": "Complete your first workout",
    "awardedAt": "2025-02-26T14:32:10"
  },
  {
    "id": 2,
    "medalName": "LIGHT_WEIGHT_BABY",
    "description": "First 225 lb lift",
    "awardedAt": "2025-02-20T09:15:00"
  }
]
```

**GET /medal/api/all response (full catalog with earned status):**
```json
[
  {
    "medalName": "BAPTISM",
    "description": "Complete your first workout",
    "earned": true,
    "awardedAt": "2026-02-26T19:32:10Z"
  },
  {
    "medalName": "LIGHT_WEIGHT_BABY",
    "description": "First 225 lb lift",
    "earned": false,
    "awardedAt": null
  }
]
```

| Field | Type | Notes |
|-------|------|-------|
| medalName | string | Enum name (e.g. BAPTISM) |
| description | string | How to earn the medal |
| earned | boolean | true if the user has already earned it |
| awardedAt | string \| null | ISO 8601 UTC when earned; null if not earned |

---

## Architecture

| Component | Purpose |
|-----------|---------|
| `MedalName` | Enum of all medal types |
| `UserMedal` | Entity (id, user_id, medal_name, awarded_at) |
| `UserMedalRepository` | Persistence + `existsByUserSubIdAndMedalName` |
| `MedalService` | Check logic and award logic for all medals |

`MedalService` is invoked from various services when relevant events occur (workout posted, post created, like added, etc.).

---

## Workout Medals

Triggered from **SessionLogService** when a workout is created (`createSessionLog` or `createNewSessionLog`).

| Medal | Condition |
|-------|-----------|
| **BAPTISM** | Complete first workout |
| **NO_PAIN_NO_GAIN** | Complete 5 workouts |
| **CALL_ME_CLOTH_THE_WAY_I_STAY_IRONED** | Complete 10 workouts |
| **LIGHT_WEIGHT_BABY** | First 225 lb single lift |
| **VETERAN** | Log 50 workouts |
| **EVERYONE_WANTS_TO_BE_A_BODY_BUILDER** | First 405 lb lift |
| **BUT_NOBODY_WANNA_LIFT_THIS_HEAVY_WEIGHT** | First 500 lb lift |
| **MENTZER_FLOW** | Workout under 30 minutes |
| **FREQUENT_FLIER** | Log 25 workouts |
| **YEA_I_WORK_HERE** | Log 100 workouts |
| **GYM_RAT** | Total gym time exceeds 24 hours |
| **ZHE_PUMP_IS_ZHE_BEST_FEELING** | Complete a chest movement (areaOfActivation contains "chest") |
| **STICK_FIGURE** | 5 consecutive workouts without legs |
| **UPSIDEDOWN_CHIP** | Complete a back movement (lats/back) |
| **AINT_NOTHING_BUT_A_PEANUT** | First 335 lb lift |
| **ONE_TIRED_SOB** | Workout 3+ hours |
| **NOT_A_CREATURE_WAS_STIRRING** | Workout before 5 am |
| **THE_GYM_IS_MY_CHURCH** | Workout on Sunday |
| **WEREWOLF** | Workout between 9pm–11:59pm |
| **SAME_TIME_TOMORROW** | Same hour 5 days in a row |
| **PLATES_BEFORE_DATES** | Workout on Valentine's Day (Feb 14) |
| **SIR_MAX_ALOT** | 20+ total sets in one workout |
| **MUSCLE_IS_THE_BEST_GIFT** | Workout on Christmas (Dec 25) |
| **IM_DRESSED_AS_AN_OLYMPIAN** | Workout on Halloween (Oct 31) |
| **SAME_ANIMAL_DIFFERENT_BEAST** | First 100 lb lift |
| **ITS_ABOUT_HOW_YOU_USE_IT** | Workout max weight ≤10 lbs |
| **UNICORN** | Complete a leg movement (quads/glutes/hamstrings/calves) |
| **GLUTTON_FOR_PUNISHMENT** | 2 workouts in the same day |

---

## Post Medals

Triggered from **PostService.createPost** when a user creates a post.

| Medal | Condition |
|-------|-----------|
| **NARCISSUS** | 50 posts |
| **PAPARAZZI_LOVER** | 10 posts with pictures |
| **POEMS_OVER_PRS** | 10 posts without pictures |

---

## Comment Medal

Triggered from **PostService.patchPost** when a comment is added.

| Medal | Condition |
|-------|-----------|
| **HYPE_MAN** | 25 comments |

---

## Like Medal

Triggered from **PostService.patchPost** when a like is added.

| Medal | Condition |
|-------|-----------|
| **INSPIRATION** | 50 likes on a single post (awarded to post author) |

---

## Follower Medals

Triggered from **FollowService.follow** when a user gains a new follower (awarded to the followee).

| Medal | Condition |
|-------|-----------|
| **THIS_IS_SPARTA** | 300 followers |
| **I_LOVE_BEING_LOVED** | 100 followers |

---

## Schedule Medal

Triggered from **WorkoutScheduleService** when a user creates or updates a workout schedule.

| Medal | Condition |
|-------|-----------|
| **PLAN_PLAN_PLAN** | Update the schedule |

---

## Delete Medal

Triggered from **SessionLogService.deleteSessionLogById** when a user deletes a logged workout.

| Medal | Condition |
|-------|-----------|
| **WE_DONT_TALK_ABOUT_THAT** | Delete a logged workout |

---

## Implementation Details

### Movement Area Checks

Workout medals that depend on movement type (chest, back, legs) use `Movement.areaOfActivation`. The seeder uses values like "Chest", "Lats", "Quads", "Glutes", "Hamstrings", "Calves". Checks are case-insensitive and use `contains()`.

### Consecutive Day Logic

- **STICK_FIGURE**: Uses `hasConsecutiveWorkoutsWithoutLegs(subId, 5)` to find 5 consecutive calendar days with workouts, none of which include leg movements.
- **SAME_TIME_TOMORROW**: Uses `hasSameTimeStreak(subId, 5)` to find 5 consecutive days where user logged a workout in the same hour each day.

### Time / Date Logic

- Gym time: `SessionLog.timeSpentInGym` (seconds).
- Workout date/time: `SessionLog.date` (Instant), converted to system default zone for time-of-day and day-of-week checks.

---

## Database

- **Table**: `user_medals` (id, user_id, medal_name, awarded_at)
- **Migration**: `src/main/resources/db/migration/create-user-medals.sql`

---

## Files Modified

| File | Changes |
|------|---------|
| `MedalName.java` | Enum of all medal types |
| `UserMedal.java` | Entity |
| `UserMedalRepository.java` | Persistence + exists check |
| `MedalService.java` | All medal check and award logic |
| `SessionLogService.java` | Calls `checkAndAwardMedals` on create, `checkDeleteMedal` on delete |
| `PostService.java` | Calls `checkPostMedals`, `checkPostLikeMedals`, `checkCommentMedals` |
| `FollowService.java` | Calls `checkFollowerMedals` |
| `WorkoutScheduleService.java` | Calls `checkScheduleMedals` |
| `PostRepository.java` | Added count methods for post medals |
| `CommentRepository.java` | New; `countByUser_SubId` for comment medal |
| `SessionLogRepository.java` | Added `countByUser_SubId` |
