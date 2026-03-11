# Notification Package

In-app notifications.

## What's Here

| File | Purpose |
|------|---------|
| **Notification** | Entity: user, type, message, isRead, actorSubId (for FOLLOW) |
| **NotificationType** | Enum: NEMESIS_POST, LEVEL_UP, MEDAL_EARNED, FOLLOW |
| **NotificationService** | Create, get paginated, mark read |
| **NotificationController** | GET notifications, PATCH mark read |
| **NotificationResponse** | DTO: id, type, message, read, actorSubId (FOLLOW: follower's sub) |
| **NotificationPageCache** | Cache DTO for paginated results |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/notification/api/` | Paginated notifications for current user |
| PATCH | `/notification/api/{id}/read` | Mark as read |

## Caching

- Notifications paginated by user; 90 sec TTL.
- Invalidated on new notification or mark-as-read.

## Types

- **FOLLOW** – Someone followed you; `actorSubId` = follower's sub.
- **NEMESIS_POST** – Your nemesis posted.
- **MEDAL_EARNED** – You earned a medal.
- **LEVEL_UP** – Rank/level up.

## Who Creates Notifications

- **FollowService** – FOLLOW on new follow.
- **MedalService** – MEDAL_EARNED when medal awarded.
- **PostService** – NEMESIS_POST when nemesis posts.
- **UserService** – LEVEL_UP (if used).
