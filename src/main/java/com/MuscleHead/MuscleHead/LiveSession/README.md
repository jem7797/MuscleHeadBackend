# LiveSession Package

Live workout sessions where a host can invite a guest to workout together.

**Invites (data model, API, JSON shapes, `hostUserName`):** see [INVITES.md](./INVITES.md).

## What's here (files and responsibilities)

| File | Responsibility |
|------|----------------|
| **LiveSessionController.java** | REST layer: `/api/live-sessions` routes, JWT user from `SecurityUtils`, delegates to the service. |
| **LiveSessionService.java** | Business logic: create session, send/accept/decline invites, end session, load session details and pending invites; maps entities to response DTOs. |
| **LiveWorkoutSession.java** | JPA entity `live_workout_sessions`: host/guest IDs, denormalized `hostUserName`, session status, timestamps. |
| **LiveWorkoutSessionRepository.java** | Persistence for `LiveWorkoutSession` (save, findById). |
| **SessionInvite.java** | JPA entity `session_invites`: link to session, from/to user IDs, `hostUserName`, message, invite status, `sentAt`. |
| **SessionInviteRepository.java** | Persistence for invites; `findPendingInvitesForUser` for the pending-invites API. |
| **LiveSessionExercise.java** | JPA entity for per-user exercise rows during a live session (session, user, exercise fields). |
| **LiveSessionExerciseRepository.java** | Loads exercises by session for `getSession`. |
| **LiveSessionExerciseDto.java** | JSON-friendly exercise shape used inside `SessionDetailsResponse`. |
| **InviteRequest.java** | Request body for `POST .../invite` (`toUserId`, optional `message`). |
| **CreateSessionResponse.java** | Response body for `POST .../create` (session id, host ids, status, timestamps, `hostUserName`). |
| **PendingInviteResponse.java** | One pending invite in the list returned by `GET .../invites/pending`. |
| **SessionDetailsResponse.java** | Full session payload for `GET .../{sessionId}` (metadata + host/guest exercise lists). |
| **README.md** | Package overview, endpoint list, high-level flow. |
| **INVITES.md** | Invite-specific docs: IDs, denormalized names, API examples, per-file breakdown for the invite path. |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/api/live-sessions/create` | Create session (host) |
| POST | `/api/live-sessions/{sessionId}/invite` | Send invite (body: toUserId, message) |
| POST | `/api/live-sessions/invites/{inviteId}/accept` | Accept invite |
| POST | `/api/live-sessions/invites/{inviteId}/decline` | Decline invite |
| POST | `/api/live-sessions/{sessionId}/end` | End session (host only) |
| GET | `/api/live-sessions/{sessionId}` | Get session with host/guest exercises |
| GET | `/api/live-sessions/invites/pending` | Pending invites for current user |

## Flow

1. Host creates session → status PENDING.
2. Host sends invite to another user.
3. Guest accepts → guest_user_id set, status in_progress; or declines.
4. Host ends session → status ENDED.
5. LiveSessionExercise rows log exercises during the session (per user).

## Auth

- All endpoints require JWT; userId from token, not request body.
