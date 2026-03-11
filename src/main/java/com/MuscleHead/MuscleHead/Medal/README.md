# Medal Package

Achievement badges (medals) awarded for user actions.

## What's Here

| File | Purpose |
|------|---------|
| **MedalName** | Enum of medal types (BAPTISM, NO_PAIN_NO_GAIN, etc.) |
| **UserMedal** | Entity: user, medal, awardedAt |
| **UserMedalRepository** | Find by user, exists checks |
| **MedalService** | Check-and-award logic for all medal types |
| **MedalResponse** | DTO for newly awarded medals |
| **MedalCatalogItem** | Catalog of medals with descriptions |

## How Medals Are Awarded

**MedalService** is called by other services after relevant actions:

- **SessionLogService** – `checkAndAwardMedals` on session create/update (workout count, consistency, etc.)
- **PostService** – `checkPostMedals`, `checkPostLikeMedals`, `checkCommentMedals`
- **FollowService** – `checkFollowerMedals`
- **WorkoutScheduleService** – `checkScheduleMedals`

Each check determines if the user qualifies for a medal; if so, it's awarded and a notification is created.

## Medal Types (examples)

- BAPTISM – First workout
- NO_PAIN_NO_GAIN – 5 workouts
- GYM_RAT – 24 hours in gym
- SAME_TIME – Workouts at same time 5 days
- STICK_FIGURE – 5 consecutive days
- Follower/post/comment-based medals

## Interactions

- **NotificationService** – Creates MEDAL_EARNED notification on award.
- **Post** – Trophy posts can showcase achievements.
