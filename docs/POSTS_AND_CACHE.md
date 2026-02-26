# Posts, Feed, and Caching

This document explains the posts feature, feed logic, S3 image storage, and caching strategy implemented in the MuscleHead backend.

---

## Overview

The posts module provides:

- **Create posts** – Save to DB and cache immediately; images stored in S3
- **Read post by ID** – Cache-first, then DB
- **Feed** – Posts only from users the current user follows, with a cached follow list (15 min TTL)
- **Post images** – Stored in S3; presigned URLs for upload and download

---

## Architecture

### Post Entity

- **PK:** `postId` (auto-generated)
- **FK:** `user` (author, references `sub_id`)
- Fields: `imageLink` (S3 object key), `caption`, `score`, `timestamp`, `likeCount`, `commentCount`, `comments`

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/posts/api/presigned-image-url` | Get presigned upload URL for post image (auth required) |
| POST | `/posts/api` | Create a post (auth required) |
| GET | `/posts/api/{id}` | Get a single post by ID |
| GET | `/posts/api/feed` | Get feed of posts from followed users (auth required, paginated) |

---

## Caching Strategy

### Redis (Upstash)

All cache entries use Redis with configurable TTLs in `application.properties`:

- `post.cache.ttl-seconds=3600` (1 hour)
- `following.cache.ttl-seconds=900` (15 minutes)

### Cache Keys

| Key Pattern | Value | Purpose |
|-------------|-------|---------|
| `post:{postId}` | JSON `PostResponse` | Avoid DB reads for single-post fetches |
| `following:{followerSubId}` | JSON array of `sub_id` strings | Avoid DB reads when building feed (TTL: 15 min) |

### Why Cache?

1. **Post reads** – Single-post lookups can be very frequent; cache reduces DB load.
2. **Following list** – The feed checks who the user follows. That list changes less often than posts are requested.
3. **Feed performance** – Once followees are cached, the feed query only hits the DB for posts, not for follows.

---

## Flow Diagrams

### Create Post

```
Client → POST /posts/api → Controller → PostService.createPost()
    → Save Post to DB
    → Increment user.number_of_posts
    → Serialize PostResponse to JSON
    → Redis SETEX post:{postId} (TTL 1h)
    → Return PostResponse
```

### Read Post by ID

```
Client → GET /posts/api/{id} → Controller → PostService.getPostById()
    → Redis GET post:{postId}
    → If hit: deserialize and return (no DB)
    → If miss: PostRepository.findById()
        → If not found: 404
        → If found: serialize to JSON, SETEX, return
```

### Feed

```
Client → GET /posts/api/feed → Controller → PostService.getFeedForUser(subId)
    → getFollowedSubIds(subId):
        → Redis GET following:{subId}
        → If hit: deserialize list and return
        → If miss: FollowRepository.findFolloweeSubIdsByFollowerSubId()
        → Redis SETEX following:{subId} (TTL 15 min)
    → If follow list empty: return empty page
    → PostRepository.findByUser_Sub_idInOrderByTimestampDesc(followedSubIds, pageable)
    → Map Post → PostResponse, return page
```

---

## Design Decisions

### 1. Cache-First Read

For both posts and the following list, we check the cache before the DB. This optimizes for the common case where data is already cached.

### 2. Write-Through on Create

When creating a post, we write to the DB first, then cache the `PostResponse`. This keeps the cache consistent and avoids a cache miss on the next read.

### 3. TTL Instead of Manual Invalidation

We use TTL so cache entries expire automatically. This avoids unbounded growth and ensures stale data is eventually refreshed.

### 4. Feed Filtering in DB

The feed is filtered by `user.sub_id IN (followedSubIds)` in the database. This avoids loading all posts and filtering in memory.

---

## Cache Clearing / Invalidation

### Current Behavior

Cache entries are invalidated only by **TTL** (1 hour). On follow/unfollow, the `following:{subId}` entry is **not** invalidated, so the feed can be stale until the TTL expires.

### Recommended: Invalidate on Follow/Unfollow

When a user follows or unfollows someone, the `following:{followerSubId}` cache entry should be cleared so the next feed request uses the fresh list.

**Implementation sketch:**

1. In the Follow service (or wherever follow/unfollow is handled), inject `RedisService`.
2. After a successful follow or unfollow, delete the cache key:
   ```java
   redisService.delete("following:" + followerSubId);
   ```
3. Add `delete(String key)` to `RedisService`:
   ```java
   public void delete(String key) {
       try (Jedis jedis = jedisPool.getResource()) {
           jedis.del(key);
       }
   }
   ```

### Optional: Post Cache on Update/Delete

If you add update or delete for posts:

- **Update** – Invalidate `post:{postId}` (or overwrite with the new data).
- **Delete** – Invalidate `post:{postId}` so a later read returns 404 from the DB instead of stale cache.

---

## S3 Post Images

Post images are stored in the same S3 bucket as profile pics (`aws.s3.bucket`), under the prefix `posts/`.

### Flow

1. **Upload image**
   - Client calls `POST /posts/api/presigned-image-url` (no body)
   - Backend returns `{ uploadUrl, objectKey }` where `objectKey` = `posts/{userId}/{uuid}.jpg`
   - Client PUTs the image bytes to `uploadUrl`

2. **Create post**
   - Client calls `POST /posts/api` with `{ imageLink: objectKey, caption: "..." }`
   - Backend stores the S3 object key in `Post.imageLink`

3. **Display image**
   - When returning `PostResponse` (create, read, feed), the backend converts the S3 object key to a presigned download URL
   - `imageLink` in the response is a ready-to-use URL (expires in 15 min)

### Object Key Convention

- `posts/{userId}/{uuid}.jpg`
- Example: `posts/abc123-cognito-sub/550e8400-e29b-41d4-a716-446655440000.jpg`

### Response Enrichment

Cached `PostResponse` stores the raw S3 object key. At response time, the backend calls `enrichWithImageUrl()` to replace the key with a fresh presigned download URL so the client always gets a displayable URL.
