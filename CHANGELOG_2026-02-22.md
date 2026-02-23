# Changelog – February 22, 2026

Summary of backend changes from the past 24 hours.

---

## User

### Partial updates (PATCH)
- Added `UpdateUserRequest` DTO with optional fields for partial updates
- **Endpoint:** `PATCH user/api/{subId}`
- Only non-null fields are applied; send only what changed
- Supports: `username`, `email`, `first_name`, `height`, `weight`, `show_weight`, `show_height`, `stat_tracking`, `privacy_setting`, `profilePicUrl`, `nattyStatus`, `bio`, `workoutSchedule`, `nemesisSubIds`

### Current user refresh
- **Endpoint:** `GET user/api/me`
- Returns the authenticated user’s profile using the JWT
- Use for pull-to-refresh or when returning to the profile screen

### User search
- **Endpoint:** `GET user/api/search?q={query}&page=0&size=10`
- Case-insensitive partial match on username
- Requires at least 2 characters
- Paginated (default size 10)

---

## Nemesis

### PATCH support
- Added `nemesisSubIds` to `UpdateUserRequest`
- **Payload:** `{ "nemesisSubIds": ["sub-id-1", "sub-id-2"] }`
- Sending `[]` clears all nemesis; omitting the field leaves it unchanged

### Remove nemesis
- **Endpoint:** `DELETE user/api/{subId}/nemesis/{nemesisSubId}`
- Removes a single nemesis from a user’s list

### Nemesis in user response
- Nemesis is always included in user responses (empty array when none)
- Returned as `UserSummary` objects: `subId`, `username`, `profilePicUrl`
- `FetchType.EAGER` used so nemesis loads with the user

### Database constraint
- Unique constraint changed from `nemesis_sub_id` to `(user_sub_id, nemesis_sub_id)`
- Multiple users can now have the same nemesis

---

## Follow

### Follow entity and API
- **Entity:** `Follow` with `follower` and `followee` (both `@ManyToOne` to `User`)
- **Endpoints:**
  - `POST follow/api/follow/{followeeSubId}` – follow a user
  - `DELETE follow/api/unfollow/{followeeSubId}` – unfollow
  - `GET follow/api/followers/{subId}` – list followers
  - `GET follow/api/following/{subId}` – list following
  - `GET follow/api/check?follower=...&followee=...` – check if following
- `UserSummary` DTO used for follower/following lists
- `number_of_followers` and `number_following` updated on follow/unfollow

---

## Notifications

### Follow notifications
- **Entity:** `Notification` with `recipient`, `actor`, `type`, `read`, `createdAt`
- **Endpoints:**
  - `GET notification/api/` – current user’s notifications (paginated)
  - `PATCH notification/api/{id}/read` – mark as read
- A `FOLLOW` notification is created when someone follows a user
- Response includes `actor` as `UserSummary` for profile navigation

---

## S3

### Presigned URLs
- **Service:** `S3Service` – generates presigned upload and download URLs
- **Endpoint:** `POST s3/api/presigned-url`
- **Payload:** `{ "objectKey": "users/{subId}/profile.jpg", "operation": "UPLOAD" }` or `"DOWNLOAD"`
- Credentials via `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` (env or `~/.aws/credentials`)

### Configuration
- Added `.env` and `.env.example` for AWS credentials
- `aws.s3.bucket`, `aws.s3.region`, `aws.s3.presigned-url-expiry-minutes` in `application.properties`

---

## Other

### User stats backfill
- Added `backfill-user-stats.sql` to sync `lifetime_weight_lifted`, `highest_weight_lifted`, `lifetime_gym_time`, `xp` from `workout_sessions`
- For users whose stats were not updated by `SessionLogService`

### Bio field
- Added `bio` to `User` (nullable)
- Included in PATCH and POST

### Application properties
- Corrected port setting: `server.port=8082` (was `port=8081`)

---

## API summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `user/api/me` | Current user profile (refresh) |
| PATCH | `user/api/{subId}` | Partial user update |
| DELETE | `user/api/{subId}/nemesis/{nemesisSubId}` | Remove nemesis |
| GET | `user/api/search?q=...` | Search users by username |
| POST | `follow/api/follow/{followeeSubId}` | Follow user |
| DELETE | `follow/api/unfollow/{followeeSubId}` | Unfollow user |
| GET | `follow/api/followers/{subId}` | List followers |
| GET | `follow/api/following/{subId}` | List following |
| GET | `notification/api/` | Current user notifications |
| PATCH | `notification/api/{id}/read` | Mark notification read |
| POST | `s3/api/presigned-url` | Get presigned S3 URL |
