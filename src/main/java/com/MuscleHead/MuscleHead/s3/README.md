# S3 Package

Presigned URL generation for S3 uploads and downloads.

## What's Here

| File | Purpose |
|------|---------|
| **S3Service** | `generatePresignedUploadUrl`, `generatePresignedDownloadUrl` |
| **S3Controller** | `POST /s3/api/presigned-url` – generic upload/download URL |
| **PresignedUrlRequest** | objectKey, operation (UPLOAD/DOWNLOAD) |
| **PresignedUrlResponse** | url, objectKey |

## How It Works

1. Client requests a presigned URL with an `objectKey`.
2. Backend generates a time-limited URL (default 15 min) using AWS S3 Presigner.
3. Client uploads/downloads directly to/from S3; no image bytes pass through the backend.

## Who Calls It

- **PostController** – `POST /posts/api/presigned-image-url` – generates `posts/{subId}/{uuid}.jpg` and returns upload URL for post images.
- **S3Controller** – `POST /s3/api/presigned-url` – client provides objectKey; used for profile pics and other uploads.

## Why Presigned URLs

- Avoids proxying large files through the backend.
- S3 handles storage and bandwidth.
- Backend only validates auth and returns a short-lived URL.

## AWS Setup

- Uses `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` from env/`.env`.
- Bucket and region from `application.properties` (`aws.s3.bucket`, `aws.s3.region`).
