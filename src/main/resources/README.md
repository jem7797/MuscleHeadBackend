# Resources

Static resources and configuration.

## Contents

| Path | Purpose |
|------|---------|
| **application.properties** | Main config: DB, Cognito, S3, Redis, cache TTLs |
| **META-INF/spring.factories** | Registers DotenvEnvironmentPostProcessor |
| **db/migration/** | SQL migration scripts for Supabase |

## Configuration Notes

- **Database** – Supabase PostgreSQL; use pooler username `postgres.{projectRef}`.
- **Redis** – Upstash; `UPSTASH_REDIS_URI` from env.
- **Cache TTLs** – Per-domain (user, post, feed, notifications, etc.) in properties.
