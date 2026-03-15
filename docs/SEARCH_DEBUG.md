# Search Feature Debug Guide

## Backend Logging (Added)

The search endpoint now logs:

1. **Request received:** `[SEARCH] Request received | q=... | page=... | size=...`
2. **Response sent:** `[SEARCH] Returning success|empty | contentSize=... | totalElements=... | responseLength=... | responsePreview=...`

**If you see these logs:** The controller is reached and returning JSON. The response preview shows the first 200 chars of what's being sent.

**If you do NOT see these logs:** The request never reached the controller. Likely causes:
- 401 Unauthorized (missing/invalid JWT) – check JWT filter logs
- Wrong URL (404)
- CORS or network error

---

## Backend Search Endpoint

| Property | Value |
|----------|-------|
| **Method** | GET |
| **URL** | `/user/api/search` |
| **Full path** | `http://localhost:8082/user/api/search` (or your Railway URL + `/user/api/search`) |
| **Auth** | **Required** – JWT in `Authorization: Bearer <token>` |
| **Query params** | `q` (optional), `page` (default 0), `size` (default 10) |

**Example:** `GET /user/api/search?q=john&page=0&size=10`

---

## Auth Failure (401)

Search requires authentication. If the token is missing or invalid:
- Spring Security returns **401** with an **empty body** (no JSON)
- The frontend tries to parse it as JSON → "invalid JSON, response starts with none" or similar

**Fix:** Ensure the frontend sends `Authorization: Bearer <idToken>` on every search request.

---

## Frontend Changes to Add

Find your search API call (search for `search`, `user/api`, or `/search`) and add:

```javascript
// BEFORE (problematic - fails on empty/non-JSON):
const response = await fetch(url);
const data = await response.json();  // Crashes if body is empty or non-JSON

// AFTER (safe - logs raw response first):
const response = await fetch(url, {
  headers: {
    'Authorization': `Bearer ${idToken}`,
    'Content-Type': 'application/json'
  }
});

const text = await response.text();
console.log('Raw search response:', text);
console.log('Search response status:', response.status);

if (!response.ok) {
  console.error('Search failed:', response.status, text);
  return { content: [], totalElements: 0 };
}

if (!text || text.trim() === '') {
  console.warn('Search returned empty body');
  return { content: [], totalElements: 0 };
}

try {
  const data = JSON.parse(text);
  return data;
} catch (e) {
  console.error('Search returned invalid JSON:', text.substring(0, 100), e);
  return { content: [], totalElements: 0 };
}
```

---

## URL Checklist

Frontend URL must exactly match:
- `https://your-backend.railway.app/user/api/search` (production)
- `http://localhost:8082/user/api/search` (local)

Common mistakes:
- Missing `/api/` → `/user/search` (wrong)
- Extra slash → `/user/api/search/` (might work, avoid)
- Wrong base URL (e.g. frontend URL instead of backend)

---

## What "response starts with none" Means

- **Empty body:** `response.json()` on empty body → parse error. Some runtimes report "none" or "undefined".
- **401/403 with no body:** Auth failure returns empty body.
- **HTML error page:** Proxy/gateway returns HTML error page; parsing as JSON fails.
- **Literal "none":** Unlikely from Spring Boot; could be from a proxy or different service.

---

## Run and Test

1. Start backend: `./mvnw spring-boot:run`
2. Trigger search from the app
3. **Backend console:** Look for `[SEARCH] Request received` and `[SEARCH] Returning`
4. **Browser console:** Look for `Raw search response:` – that shows exactly what the frontend received

Share the `Raw search response:` output to diagnose further.
