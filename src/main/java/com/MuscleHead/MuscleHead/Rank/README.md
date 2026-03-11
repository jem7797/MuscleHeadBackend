# Rank Package

User rank/level system.

## What's Here

| File | Purpose |
|------|---------|
| **Rank** | Entity: id, level, name (e.g. "Beginner", "Intermediate") |
| **RankSeeder** | Seeds rank tiers on startup |

## How It Works

- **User** has a `Rank` (ManyToOne).
- Rank represents level/name; typically driven by XP or similar metrics.
- RankSeeder ensures standard tiers exist in the DB.
