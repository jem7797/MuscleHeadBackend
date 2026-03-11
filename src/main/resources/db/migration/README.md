# Database Migrations

SQL migration scripts for Supabase/PostgreSQL.

## How They're Used

- **Hibernate `ddl-auto=update`** – In dev, Hibernate can create/alter tables from entities.
- **Manual runs** – For production or controlled schema changes, run scripts in the Supabase SQL editor.

## Migration Files

| File | Purpose |
|------|---------|
| `create-notifications.sql` | Notifications table |
| `create-worked-muscles.sql` | Worked muscles table |
| `create-follow-requests.sql` | Follow requests for private accounts |
| `create-live-workout-sessions.sql` | Live session tables |
| `add-notification-actor-sub-id.sql` | actor_sub_id for FOLLOW notifications |
| `add-profile-pic-version.sql` | Cache-busting for profile pics |
| `add-worked-muscles-unique-constraint.sql` | Unique constraint on worked muscles |
| `add-user-gender.sql` | Gender field on users |
| Others | Various schema updates |

## Running Migrations

1. Open Supabase Dashboard → SQL Editor.
2. Paste script content.
3. Execute.

**Note:** Some migrations drop tables or have destructive changes. Review before running.
