## 7. Security

---

## What is Security?

Security is **protecting your application and data** from unauthorized access, attacks, and abuse.

```
Without Security:
→ Anyone can access any API ❌
→ Passwords stored as plain text ❌
→ Anyone can spam your APIs ❌
→ Sensitive data exposed ❌

With Security:
→ Only authenticated users access APIs ✅
→ Passwords encrypted ✅
→ Rate limiting prevents abuse ✅
→ Sensitive data protected ✅
```

---

## Simple Analogy

```
Without Security:
House with no locks
→ Anyone can walk in
→ Anyone can take anything ❌

With Security:
House with locks + alarm + guard
→ Only authorized people enter ✅
→ Intruders detected and blocked ✅
```

---

## Subtopics We Used

---

### 7.1 JWT Authentication

**What:**
```
JWT = JSON Web Token
3 parts separated by dots:
Header.Payload.Signature

Example:
eyJhbGc.eyJzdWIi.SflKxwRJ
```

**How JWT works:**
```
1. User logs in with email + password
2. Server verifies credentials
3. Server creates JWT with user info
4. Server signs JWT with secret key
5. Returns JWT to user

Next request:
1. User sends JWT in header
   Authorization: Bearer eyJhbGc...
2. Server verifies signature
3. Extracts user info from token
4. Allows or rejects request
```

**JWT Structure:**
```
Header:
{
    "alg": "HS256",
    "typ": "JWT"
}

Payload:
{
    "sub": "hari@gmail.com",
    "iat": 1234567890,
    "exp": 1234654290
}

Signature:
HMACSHA256(base64(header) + base64(payload), secret)
```

**Why JWT:**
```
✅ Stateless → no session stored on server
✅ Scalable → any server can verify
✅ Self contained → user info in token
✅ Expiry built in
✅ Industry standard
```

**Problems with JWT:**
```
❌ Cannot invalidate before expiry
❌ Token stolen → attacker has access
❌ Large payload increases request size
```

**How to improve:**
```
→ Short expiry (1 hour not 24 hours)
→ Refresh token mechanism
→ Token blacklist in Redis
→ HTTPS only to prevent interception
```

---

### 7.2 Password Encryption (BCrypt)

**What:**
```
BCrypt is a password hashing algorithm
Converts password to irreversible hash
Even if DB is hacked → passwords safe
```

**How it works:**
```
Plain password: "123456"
BCrypt hash: "$2a$10$N9qo8uLOickgx..."

Properties:
→ Same password → different hash each time (salt)
→ Hash cannot be reversed
→ Slow by design → brute force takes forever
```

**Why BCrypt:**
```
✅ Industry standard for password hashing
✅ Salt prevents rainbow table attacks
✅ Slow → brute force computationally expensive
✅ Built into Spring Security
```

**Problems without BCrypt:**
```
❌ Plain text passwords → DB hack = all passwords exposed
❌ MD5/SHA1 → fast → brute force easy
❌ No salt → rainbow table attacks work
```

**How to improve:**
```
→ Increase BCrypt rounds (cost factor)
→ Add password complexity requirements
→ Add password history (can't reuse old passwords)
→ Add account lockout after failed attempts
```

---

### 7.3 Spring Security Filter Chain

**What:**
```
Chain of filters that process every request
before it reaches your controller
```

**Our filter chain order:**
```
Request comes in
→ RateLimitFilter      → check rate limit
→ JwtAuthFilter        → verify JWT token
→ SecurityConfig       → check permissions
→ Controller           → handle request
→ Response goes out
```

**How JwtAuthFilter works:**
```kotlin
// For every request:
1. Extract token from Authorization header
2. Validate token signature
3. Extract email from token
4. Load user from DB
5. Set authentication in SecurityContext
6. Continue to next filter
```

**SecurityConfig — who can access what:**
```kotlin
.requestMatchers("/api/auth/**").permitAll()
→ signup and login → no token needed

.requestMatchers("/actuator/health/**").permitAll()
→ health check → no token needed

.requestMatchers("/swagger-ui/**").permitAll()
→ swagger docs → no token needed

.anyRequest().authenticated()
→ everything else → token required
```

**Why Filter Chain:**
```
✅ Security applied to every request automatically
✅ No need to add security code in every controller
✅ Clean separation of concerns
✅ Easy to configure per endpoint
```

**Problems without Filter Chain:**
```
❌ Must add security check in every controller
❌ Easy to forget one endpoint
❌ Inconsistent security
❌ Code duplication
```

---

### 7.4 Role Based Access

**What:**
```
Different users have different permissions
Based on their role
```

**How we implemented:**
```kotlin
enum class Role {
    USER,
    ADMIN
}

// In User entity:
val role: Role = Role.USER
```

**Current state:**
```
All users → USER role
→ Can access all APIs

Future improvement:
ADMIN role → can delete videos
           → can manage users
           → can see analytics
```

**Why Role Based Access:**
```
✅ Fine grained permissions
✅ Admin features protected
✅ User can't access admin APIs
✅ Easy to extend
```

---

### 7.5 Rate Limiting as Security

**What:**
```
Rate limiting also acts as security measure
Prevents brute force and spam attacks
```

**Security scenarios it handles:**
```
Brute force login:
→ Attacker tries 1000 passwords/minute
→ Rate limit: 10 login/minute
→ Attack blocked ✅

Signup spam:
→ Bot creates 1000 accounts/minute
→ Rate limit: 5 signup/minute
→ Spam blocked ✅

API scraping:
→ Competitor scrapes all your data
→ Rate limit: 30 requests/minute
→ Scraping slowed down ✅
```

---

### 7.6 CORS Configuration

**What:**
```
CORS = Cross Origin Resource Sharing
Controls which domains can call your API
```

**Why needed:**
```
Without CORS:
→ Any website can call your API
→ Malicious site can steal user data

With CORS:
→ Only allowed domains can call your API
→ Other domains blocked by browser
```

**How to add to our project:**
```kotlin
@Bean
fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration()
    config.allowedOrigins = listOf("http://localhost:3000")
    config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
    config.allowedHeaders = listOf("*")
    config.allowCredentials = true
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)
    return source
}
```

**Currently not configured → improvement needed!**

---

### 7.7 HTTPS

**What:**
```
Encrypts data between client and server
Prevents man-in-the-middle attacks
```

**Current state:**
```
Development → HTTP (localhost)
Production  → Must use HTTPS ✅
```

**Why HTTPS:**
```
✅ JWT token encrypted in transit
✅ Passwords encrypted in transit
✅ User data protected
✅ Required for production
```

**How to add:**
```
→ Add SSL certificate (Let's Encrypt - free)
→ Configure in application.properties
→ Redirect HTTP to HTTPS
→ Handled automatically by cloud platforms
```

---

## Summary Table

| Security Feature | Problem Solved | Tech Used |
|-----------------|---------------|-----------|
| JWT | Unauthorized access | Spring Security + JWT |
| BCrypt | Password theft | Spring Security BCrypt |
| Filter Chain | Unsecured endpoints | Spring Security |
| Role Based Access | Privilege escalation | Spring Security Roles |
| Rate Limiting | Brute force, spam | Bucket4j |
| CORS | Cross site attacks | Spring Security CORS |
| HTTPS | Data interception | SSL/TLS |

---

## How Security Improved Our Project

```
Before Security:
→ Anyone accesses any API ❌
→ Passwords in plain text ❌
→ Brute force attacks possible ❌
→ API can be spammed ❌

After Security:
→ JWT protects all APIs ✅
→ BCrypt protects passwords ✅
→ Rate limiting blocks brute force ✅
→ Filter chain → no endpoint forgotten ✅
```

---

## Security Layers in Our Project

```
Layer 1 → Rate Limiting    → prevent abuse
Layer 2 → JWT Auth         → verify identity
Layer 3 → Spring Security  → check permissions
Layer 4 → Input Validation → prevent bad data
Layer 5 → BCrypt           → protect passwords
Layer 6 → HTTPS (needed)   → protect data in transit
```

---
