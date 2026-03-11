# Config Package

Configuration for security, authentication, Redis, and application-wide behavior.

## What's Here

| File | Purpose |
|------|---------|
| **SecurityConfig** | HTTP security: CORS, stateless sessions, JWT filter, public vs protected routes |
| **JwtAuthenticationFilter** | Extracts Bearer token, validates via Cognito JWKS, sets `SecurityContext` |
| **CognitoJwtValidator** | Validates JWT and extracts `sub` (user ID) and email |
| **SecurityUtils** | `getCurrentUserSub()` – reads authenticated user from `SecurityContext` |
| **UnderAgeAccessFilter** | Blocks users under 13 after JWT auth |
| **RedisConfig** | Jedis pool for Upstash Redis (from `UPSTASH_REDIS_URI`) |
| **DotenvEnvironmentPostProcessor** | Loads `.env` for local dev |
| **LandingPageController** | Serves `/` and health endpoints |

## How Auth Works

1. Client sends `Authorization: Bearer <jwt>`.
2. **JwtAuthenticationFilter** runs before controllers.
3. Token is validated against Cognito JWKS; `sub` is the user ID.
4. `UsernamePasswordAuthenticationToken` is set with `principal = sub`.
5. **SecurityUtils.getCurrentUserSub()** returns that `sub` in controllers/services.

## Why These Choices

- **Stateless** – No server-side sessions; scalable and suitable for APIs.
- **Cognito JWKS** – Public key validation instead of shared secrets.
- **Filter order** – JWT first, then UnderAge; age check needs a valid user.
- **Public routes** – Only `/`, health, user signup, minor-signup-attempt are unauthenticated.
