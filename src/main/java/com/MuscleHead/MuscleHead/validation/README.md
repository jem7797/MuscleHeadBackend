# Validation Package

Custom validation annotations and groups.

## What's Here

| File | Purpose |
|------|---------|
| **ValidBirthYear** | Ensures birth year is reasonable (e.g. not future) |
| **ValidBirthYearValidator** | Implementation of `ValidBirthYear` |
| **AwsCognitoSubId** | Ensures string matches Cognito sub format (UUID) |
| **AwsCognitoSubIdValidator** | Implementation of `AwsCognitoSubId` |
| **OnCreate** | Validation group for create-only fields |
| **OnUpdate** | Validation group for update-only fields |

## How It Works

- `@Valid` on request bodies triggers Jakarta validation.
- Custom annotations (`@ValidBirthYear`, `@AwsCognitoSubId`) run their validators.
- Groups (`OnCreate`, `OnUpdate`) let the same entity have different rules for create vs update.

## Why These Choices

- **Cognito sub validation** – Prevents invalid IDs from reaching the DB.
- **Birth year** – Prevents impossible dates and supports age checks.
- **Groups** – e.g. `sub_id` required on create but not on partial update.
