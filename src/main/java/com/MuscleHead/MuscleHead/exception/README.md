# Exception Package

Centralized error handling for the API.

## What's Here

| File | Purpose |
|------|---------|
| **GlobalExceptionHandler** | `@RestControllerAdvice` – maps exceptions to HTTP responses |
| **PostAchievementNotFoundException** | 404 when achievement not found for trophy post |
| **PostAchievementForbiddenException** | 403 when user doesn't own achievement |
| **PostAchievementConflictException** | 409 when achievement already posted |
| **UnderAgeException** | 403 when user is under 13 |
| **LiveSessionForbiddenException** | 403 when non-host tries to end a live session |

## Exception → HTTP Mapping

| Exception | Status | When |
|-----------|--------|------|
| `MethodArgumentNotValidException` | 400 | `@Valid` fails |
| `IllegalArgumentException` | 400 | Bad input / "not found" messages |
| `IllegalStateException` | 409 if "already exists", else 400 | Business rule violations |
| `*NotFoundException` | 404 | Resource missing |
| `*ForbiddenException` | 403 | Not allowed |
| `*ConflictException` | 409 | Duplicate / conflict |
| `UnderAgeException` | 403 | Under 13 |
| `RuntimeException` with "not found" in message | 404 | Generic not found |
| Other `RuntimeException` | 500 | Unexpected error |
| `Exception` | 500 | Fallback |

## Why Centralized

- Same JSON shape for all errors.
- Controllers stay focused on business logic.
- Consistent logging and status codes.
- Easy to add new exception types without touching every controller.
