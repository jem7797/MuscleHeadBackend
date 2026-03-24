# LiveSession Package

Live workout sessions where a host can invite a guest to workout together.

**Invites (data model, API, JSON shapes, `hostUserName`):** see [INVITES.md](./INVITES.md).

## What's Here

| File | Purpose |
|------|---------|
| **LiveWorkoutSession** | Entity: host, guest, status (PENDING/in_progress/ENDED) |
| **SessionInvite** | Entity: session, requester, to_user, status (pending/accepted/declined) |
| **LiveSessionExercise** | Entity: session, user, exercise name, sets, reps, weight |
| **LiveSessionService** | Create session, invite, accept/decline, end, get session |
| **LiveSessionController** | REST API |

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
