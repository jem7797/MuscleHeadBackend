# Live session invites

This document describes how invites work in the MuscleHead backend: data model, API, IDs, and why the host’s display name appears in several places.

## Overview

- A **live workout session** (`LiveWorkoutSession`) is created by a **host** while the session is `PENDING`.
- The host sends one or more **invites** (`SessionInvite`) to other users (by Cognito `sub`).
- The **recipient** lists pending invites, then **accepts** or **declines**. Accepting attaches them as `guest_user_id` and moves the session to `in_progress`.

All invite-related REST calls live under `LiveSessionController` at base path **`/api/live-sessions`**. Authentication uses the JWT; the current user is always taken from **`SecurityUtils.getCurrentUserSub()`** (Cognito `sub`), never from an arbitrary field in the body for “who am I”.

## User IDs

| Concept | Source |
|--------|--------|
| `hostUserId`, `fromUserId`, `toUserId`, `guestUserId` | Cognito **`sub`** (same as `User.sub_id` in the database) |

The **`toUserId`** in `POST .../invite` must be the invitee’s **`sub`**. If the client sends an internal ID or username, the row is stored for the wrong user and **`GET .../invites/pending`** will return nothing for the real recipient.

## Entities

### `SessionInvite` (`session_invites`)

| Field (Java) | Role |
|--------------|------|
| `id` | Invite UUID |
| `session` | FK to `LiveWorkoutSession` |
| `fromUserId` | Host’s `sub` (sender) |
| `toUserId` | Invitee’s `sub` (recipient) |
| `hostUserName` | Denormalized display name for the host (see below) |
| `message` | Optional note from host |
| `status` | `pending`, `accepted`, `declined` |
| `sentAt` | When the invite was created |

### `LiveWorkoutSession` (`live_workout_sessions`)

Stores **`hostUserName`** when the session is created so every invite and session detail response can show who is hosting without joining the `users` table on every read.

## Why `hostUserName` appears in multiple places

1. **`LiveWorkoutSession.hostUserName`** — Set in **`createSession`** from `User.username` when the host creates a session. It is the **source of truth** for “who is hosting this session” for display purposes on that row.

2. **`SessionInvite.hostUserName`** — Copied in **`sendInvite`** from `session.getHostUserName()`. Invites are loaded independently of heavy session joins; storing the name on the invite row lets **`GET /invites/pending`** return a friendly label (“who invited me”) in one query.

3. **`CreateSessionResponse.hostUserName`** — Returned to the **host** right after create so the client can show the correct label immediately without an extra `GET session`.

4. **`PendingInviteResponse.hostUserName`** — JSON field for each pending invite for the **recipient**.

5. **`SessionDetailsResponse.hostUserName`** — Included when anyone fetches session details by `sessionId`.

This is **denormalization**: the same logical fact (host’s display name) is duplicated so each API response and table row that needs it can be built simply. The tradeoff is that if a user changes their username later, old sessions/invites still show the name from when the session was created (unless you add a migration or refresh job).

## API reference

### Create session (host)

`POST /api/live-sessions/create`  
**Auth:** required.

**Response** `201` — `CreateSessionResponse`:

```json
{
  "id": "uuid",
  "hostUserId": "cognito-sub",
  "status": "PENDING",
  "createdAt": "2025-03-24T12:00:00Z",
  "hostUserName": "displayName"
}
```

### Send invite (host only)

`POST /api/live-sessions/{sessionId}/invite`  
**Auth:** required. Caller must be the session host; session must be `PENDING`.

**Body** (`InviteRequest`):

```json
{
  "toUserId": "invitee-cognito-sub",
  "message": "optional string"
}
```

**Response** `201` — empty body.

### List pending invites (recipient)

`GET /api/live-sessions/invites/pending`  
**Auth:** required.

**Response** `200` — JSON **array** (not wrapped in `{ "invites": ... }`):

```json
[
  {
    "inviteId": "uuid",
    "sessionId": "uuid",
    "fromUserId": "host-sub",
    "message": "",
    "sentAt": "2025-03-24T12:00:00Z",
    "hostUserName": "displayName",
    "status": "pending"
  }
]
```

Field names are **camelCase** (`hostUserName`, not `host_user_name`). `sentAt` is serialized as an ISO-8601 string (Jackson default for `Instant`).

### Accept invite

`POST /api/live-sessions/invites/{inviteId}/accept`  
**Auth:** required. Caller must be `toUserId` for that invite; invite must be `pending`.

**Response** `200` — empty body.

### Decline invite

`POST /api/live-sessions/invites/{inviteId}/decline`  
**Auth:** required. Same recipient rules as accept.

**Response** `200` — empty body.

## End-to-end flow

1. Host: `POST /create` → receives `sessionId` and `hostUserName` in the response.
2. Host: `POST /{sessionId}/invite` with `toUserId` = friend’s **`sub`**.
3. Friend: `GET /invites/pending` → sees `inviteId`, `sessionId`, `fromUserId`, `hostUserName`, etc.
4. Friend: `POST /invites/{inviteId}/accept` or `.../decline`.
5. After accept, both can use `GET /{sessionId}` for full session details (includes `hostUserName` and exercises).

## Implementation map

| Concern | Location |
|---------|----------|
| HTTP routes | `LiveSessionController.java` |
| Business rules + mapping to DTOs | `LiveSessionService.java` |
| Invite persistence | `SessionInvite.java`, `SessionInviteRepository.java` |
| Request body for send invite | `InviteRequest.java` |
| Pending list JSON shape | `PendingInviteResponse.java` |

## Related

- Session lifecycle (end session, exercises): see `README.md` in this package.
