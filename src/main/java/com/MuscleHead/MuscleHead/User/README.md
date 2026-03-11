# User Package

User accounts, profiles, and search.

## What's Here

| File | Purpose |
|------|---------|
| **User** | Entity: sub_id, username, email, profile, stats, privacy_setting, nemesis, etc. |
| **UserRepository** | JPA repo: findById, search (excluding hidden), visible sub_ids |
| **UserService** | Create, update, get, search, delete; user cache |
| **UserController** | REST API for user operations |
| **UpdateUserRequest** | DTO for PATCH (partial updates) |
| **BlockedEmail** | Entity for blocked signup emails |
| **MinorSignupAttemptRequest** | Records under-13 signup attempts |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/user/api/` | Create user (public) |
| GET | `/user/api/me` | Current user (auth) |
| GET | `/user/api?subId=...` or `?username=...` | Get user |
| PATCH | `/user/api/{subId}` | Partial update (auth) |
| DELETE | `/user/api/{subId}` | Delete user (auth) |
| GET | `/user/api/search?q=...` | Search users (excludes hidden) |

## Caching

- **User by ID** – Cached 1 hr; invalidated on update/delete.
- **Search** – No cache; excludes `privacy_setting = 'hidden'`.

## Privacy Settings

- **public** – Anyone can follow without request; appears in search.
- **private** – Requires follow request; appears in search.
- **hidden** – Excluded from search, feed, followers/following lists.

## Interactions

- **Follow** – UserSummary used in followers/following; FollowService invalidates follow caches.
- **Post** – User is post author; PostService uses UserRepository.
- **Rank** – User has a Rank; RankSeeder provides levels.
