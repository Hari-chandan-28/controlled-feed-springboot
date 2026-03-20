## 1. API Design

---

## What is API Design?

API Design is the process of **planning how your endpoints will look and behave** before building them.

```
Not just writing @GetMapping("/something")
But thinking:
→ What URL structure to use?
→ What data to send and receive?
→ How to handle errors?
→ How to secure it?
→ How to version it?
```

---

## Subtopics

---

### 1.1 REST API Design

**What is it?**
```
REST = Representational State Transfer
A standard way of designing APIs using HTTP methods
```

**Rules we followed:**
```
GET    → fetch data    → /api/f1/standings
POST   → create data   → /api/auth/signup
PUT    → update data   → /api/profile/update
DELETE → delete data   → /api/profile/delete
```

**Why we use it:**
```
✅ Industry standard
✅ Easy to understand
✅ Works with any frontend
✅ Stateless → each request is independent
```

**Problems without it:**
```
❌ Inconsistent URLs like /getUser, /fetchUser, /retrieveUser
❌ Confusing for frontend developers
❌ Hard to maintain
```

**In our project:**
```
/api/auth/signup     → POST (create user)
/api/profile/me      → GET (fetch profile)
/api/f1/standings    → GET (fetch standings)
/api/chat/ask        → POST (send question)
```

---

### 1.2 JWT Authentication

**What is it?**
```
JSON Web Token
A secure way to verify who is making the request
```

**How it works:**
```
User logs in → server creates token
User sends token with every request
Server verifies token → allows or rejects
```

**Why we use it:**
```
✅ Stateless → no session stored on server
✅ Scalable → works across multiple servers
✅ Secure → token signed with secret key
✅ Standard → works with any frontend/mobile
```

**Problems without it:**
```
❌ Session based auth → server stores sessions
❌ Doesn't scale → all servers need shared session
❌ More DB queries per request
```

**In our project:**
```
Login → returns JWT token
Every API → JwtAuthFilter validates token
Invalid token → 401 Unauthorized
Expired token → 401 Unauthorized
```

**How it can improve:**
```
Add refresh tokens → user stays logged in longer
Add token blacklisting → logout properly
Add role based access → ADMIN vs USER endpoints
```

---

### 1.3 Rate Limiting

**What is it?**
```
Controlling how many requests 
a user can make in a time period
```

**How we implemented:**
```
Bucket4j → token bucket algorithm
Each IP gets a bucket of tokens
Each request consumes one token
Empty bucket → 429 Too Many Requests
Tokens refill over time
```

**Why we use it:**
```
✅ Prevent abuse and spam
✅ Protect server from overload
✅ Fair usage for all users
✅ Prevent brute force attacks
```

**Problems without it:**
```
❌ One user can spam 1000 requests/second
❌ Server crashes under load
❌ Brute force password attacks possible
❌ YouTube/CricAPI quota exhausted by one user
```

**Our limits:**
```
Signup  → 5/min   (prevent fake accounts)
Login   → 10/min  (prevent brute force)
YouTube → 10/min  (protect API quota)
Feed    → 60/min  (normal usage)
Others  → 30/min  (general protection)
```

**How it can improve:**
```
Add per-user rate limiting (not just per IP)
Add different limits for FREE vs PREMIUM users
Add rate limit headers in response
→ X-RateLimit-Remaining: 29
→ X-RateLimit-Reset: 60
```

---

### 1.4 Pagination

**What is it?**
```
Returning data in small chunks instead of all at once
```

**How we implemented:**
```
GET /api/feed?page=0&size=10
→ Returns 10 videos at a time
→ Client requests next page when needed
```

**Why we use it:**
```
✅ Faster response times
✅ Less memory usage
✅ Better user experience
✅ Reduces DB load
```

**Problems without it:**
```
❌ /api/feed returns 10000 videos at once
❌ Response takes 30 seconds
❌ Frontend crashes trying to render all data
❌ DB query scans entire table
```

**How it can improve:**
```
Add cursor based pagination
→ Better performance for large datasets
→ No page number needed
→ Uses last seen ID as cursor

Add total count in response:
{
  "data": [...],
  "totalPages": 10,
  "currentPage": 0,
  "totalItems": 100
}
```

---

### 1.5 Input Validation

**What is it?**
```
Checking that the data sent by user is correct
before processing it
```

**How we implemented:**
```kotlin
@NotBlank(message = "Name is required")
val name: String

@Email(message = "Invalid email format")
val email: String

@Size(min = 6, message = "Password must be at least 6 characters")
val password: String
```

**Why we use it:**
```
✅ Prevent bad data entering database
✅ Clear error messages to user
✅ Security (prevent injection attacks)
✅ Data integrity
```

**Problems without it:**
```
❌ Empty emails stored in DB
❌ Passwords with 1 character
❌ SQL injection possible
❌ App crashes on null values
```

**How it can improve:**
```
Add more validations:
→ Genre must be F1 or CRICKET only
→ Profile picture must be jpg/png only
→ Bio max 500 characters
→ Username no special characters
```

---

### 1.6 Error Handling

**What is it?**
```
Returning clear, consistent error responses
when something goes wrong
```

**How we implemented:**
```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(e: ResourceNotFoundException) → 404

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException) → 400
}
```

**Standard error format:**
```json
{
    "status": 404,
    "error": "Not Found",
    "message": "Profile not found"
}
```

**Why we use it:**
```
✅ Consistent error format across all APIs
✅ Clear messages for frontend developers
✅ No stack traces exposed to users
✅ Easy to debug
```

**Problems without it:**
```
❌ Random error formats from different endpoints
❌ Stack traces exposed to users (security risk!)
❌ Frontend doesn't know what went wrong
❌ Hard to debug in production
```

**How it can improve:**
```
Add error codes:
{
    "status": 404,
    "errorCode": "PROFILE_NOT_FOUND",
    "message": "Profile not found for user"
}

Add request ID for tracking:
{
    "requestId": "abc-123-xyz",
    "status": 500,
    "message": "Internal server error"
}
```

---

## Summary Table

| Subtopic | Problem Solved | Our Implementation |
|----------|---------------|-------------------|
| REST Design | Consistent URLs | HTTP methods + resource naming |
| JWT Auth | Secure access | JwtAuthFilter + token validation |
| Rate Limiting | Prevent abuse | Bucket4j per IP |
| Pagination | Performance | page + size params |
| Input Validation | Bad data | @Valid + annotations |
| Error Handling | Clear errors | GlobalExceptionHandler |
---
