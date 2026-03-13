# S3 Package

Presigned URL generation for S3 uploads and downloads.

## What's Here

| File | Purpose |
|------|---------|
| **S3Service** | `generatePresignedUploadUrl`, `generatePresignedDownloadUrl` |
| **S3Controller** | `POST /s3/api/presigned-url` – generic upload/download URL |
| **PresignedUrlRequest** | objectKey, operation (UPLOAD/DOWNLOAD) |
| **PresignedUrlResponse** | url, objectKey, contentType |

## Profile Pic Upload Flow

1. Frontend: `POST /s3/api/presigned-url` with `{ objectKey: "users/{subId}/profile.jpg", operation: "UPLOAD", contentType: "application/octet-stream" }`
2. Backend returns `{ url, objectKey, contentType }`
3. Frontend: `PUT url` with the image bytes and **Header: Content-Type: application/octet-stream** (must match exactly)
4. Frontend: `PATCH /user/api/{subId}` with `{ profilePicUrl: objectKey }`
5. Backend enriches User responses: profilePicUrl is replaced with a presigned download URL

## Content-Type (Critical)

The PUT request to the presigned upload URL **must** use the exact Content-Type from the response. Otherwise S3 returns 403 SignatureDoesNotMatch. Use `application/octet-stream` if unsure.

## How It Works

1. Client requests a presigned URL with an `objectKey`.
2. Backend generates a time-limited URL (default 15 min) using AWS S3 Presigner.
3. Client uploads/downloads directly to/from S3; no image bytes pass through the backend.
4. Upload and download use the same bucket (`aws.s3.upload-bucket` defaults to `aws.s3.bucket`).

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
