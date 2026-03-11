# Follow Package

Follow relationships, follow requests (for private accounts), and related logic.

## What's Here

| File | Purpose |
|------|---------|
| **Follow** | Entity: follower ↔ followee (composite key) |
| **FollowId** | Embeddable: followerSubId + followeeSubId |
| **FollowRepository** | Find by follower/followee; exclude hidden users |
| **FollowService** | Follow, unfollow, get followers/following, isFollowing, mutual, requests |
| **FollowController** | REST API for follow operations |
| **FollowRequest** | Entity: requester, followee, status (pending/accepted/declined) |
| **FollowRequestRepository** | Pending requests by followee or requester+followee |
| **FollowRequestResponse** | DTO for pending requests |
| **UserSummary** | Light DTO: subId, username, profilePicUrl, etc. (used in lists) |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/follow/api/follow/{followeeSubId}` | Follow (or create request if private) |
| POST | `/follow/api/request/{followeeSubId}` | Create follow request explicitly |
| DELETE | `/follow/api/unfollow/{followeeSubId}` | Unfollow |
| GET | `/follow/api/followers/{subId}` | List followers (excludes hidden) |
| GET | `/follow/api/following/{subId}` | List following (excludes hidden) |
| GET | `/follow/api/check?follower=X&followee=Y` | Is X following Y? |
| GET | `/follow/api/mutual?user1=X&user2=Y` | Do X and Y follow each other? |
| GET | `/follow/api/requests` | Pending requests for current user |
| POST | `/follow/api/requests/{id}/accept` | Accept request |
| POST | `/follow/api/requests/{id}/decline` | Decline request |
| GET | `/follow/api/request-status?requester=X&followee=Y` | "pending" or "none" |

## Caching

- **Followers list** – 15 min; invalidated on follow/unfollow/accept.
- **Following list** – 15 min; same invalidation.
- **isFollowing** – 15 min; invalidated when that pair changes.
- **Mutual followers** – 15 min; invalidated when either user's follows change.

## Private Account Flow

1. User A (public) tries to follow User B (private).
2. `POST /follow` creates a follow request instead of a follow.
3. User B sees request via `GET /requests`.
4. User B accepts → Follow is created; declines → Request marked declined.

## Interactions

- **PostService** – Uses `getFollowedSubIds` for feed (cached in PostService).
- **NotificationService** – Creates FOLLOW notification on new follow.
- **MedalService** – Checks follower-based medals.
- **UserRepository** – `findVisibleUserSubIds` used for feed filtering.
