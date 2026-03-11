# Cache Package

Redis-based caching layer used across the app.

## What's Here

| File | Purpose |
|------|---------|
| **RedisService** | Wraps Jedis: `get`, `set`, `setWithTtl`, `delete`, `deleteKeysByPattern` |

## Who Uses It

| Consumer | What's Cached | TTL | Invalidation |
|----------|---------------|-----|--------------|
| UserService | User by ID | 1 hr | On update |
| PostService | Post by ID, feed, following sub_ids | 1 hr / 15 min | On post create/delete, follow/unfollow |
| NotificationService | Notification pages | 90 sec | On new notification, mark read |
| WorkedMusclesService | Per-user worked muscles | 5 min | On workout |
| FollowService | Followers list, following list, isFollowing, mutual | 15 min | On follow/unfollow/accept request |

## Why Redis

- Reduces DB load on high-read endpoints (feed, user profile, followers).
- Upstash Redis is managed and works well for serverless/stateless backends.
- TTL avoids unbounded growth; pattern delete supports per-user invalidation.

## Key Patterns

- **Cache key conventions** – `{prefix}:{id}` or `{prefix}:{id}:{page}`.
- **Invalidate on write** – Services delete relevant keys when data changes.
- **JSON storage** – Objects are serialized to JSON; each consumer parses with ObjectMapper.
