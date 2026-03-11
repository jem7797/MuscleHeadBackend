# Post Package

Posts, likes, comments, and the feed.

## What's Here

| File | Purpose |
|------|---------|
| **Post** | Entity: user, imageLink, caption, trophy, achievement, likeCount, commentCount |
| **PostRepository** | Find by user, by IDs, with achievement |
| **PostService** | Create, get, patch, delete; feed; cache |
| **PostController** | REST API for posts |
| **PostRequest** | DTO: imageLink, caption, isTrophy, achievementId |
| **PostResponse** | DTO: includes user summary, comments, like/comment counts |
| **Post/Like/** | Like entity, repo, LikeId |
| **Post/Comment/** | Comment entity, repo |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/posts/api/presigned-image-url` | Get presigned URL for post image |
| POST | `/posts/api` | Create post |
| GET | `/posts/api/{id}` | Get post by ID |
| GET | `/posts/api/feed` | Feed for current user |
| GET | `/posts/api/user/{subId}` | Posts by user |
| PATCH | `/posts/api/{id}` | Like and/or comment |
| DELETE | `/posts/api/{id}` | Delete post (author only) |

## Image Flow

1. Client calls `POST /presigned-image-url` → receives `(uploadUrl, objectKey)`.
2. Client uploads image to S3 via PUT.
3. Client calls `POST /posts` with `imageLink = objectKey`.
4. On read, `imageLink` is replaced with a presigned download URL.

## Caching

- **Post by ID** – 1 hr; invalidated on delete.
- **Feed** – 1 hr per page; invalidated on post create, follow/unfollow.
- **Following sub_ids** – 15 min; used to build feed; invalidated on follow/unfollow.

## Feed Logic

- Feed = posts from users the current user follows + own posts.
- Excludes users with `privacy_setting = 'hidden'`.
- Current user always sees their own posts.

## Interactions

- **User** – Post author; UserSummary in response.
- **Follow** – Feed uses followed sub_ids from FollowRepository.
- **Medal** – Post and comment medals checked on create.
- **Notification** – Nemesis post notifications.
