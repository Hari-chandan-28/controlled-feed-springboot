## 12. Design Patterns

---

## What are Design Patterns?

Design Patterns are **proven solutions to common software problems**.

```
Without Design Patterns:
→ Reinvent solutions every time
→ Inconsistent code structure
→ Hard to maintain
→ Hard for others to understand ❌

With Design Patterns:
→ Proven solutions used
→ Consistent structure
→ Easy to maintain
→ Any developer understands instantly ✅
```

---

## Simple Analogy

```
Building houses:
→ Architects don't redesign doors every time
→ They use standard door patterns
→ Everyone knows how a door works ✅

Writing software:
→ Developers don't redesign solutions every time
→ They use standard design patterns
→ Everyone knows how the code works ✅
```

---

## Patterns We Used

---

### 12.1 Service Layer Pattern

**What:**
```
Separate business logic into Service classes
Controllers handle HTTP
Services handle business logic
Repositories handle data
```

**How we implemented:**
```
Controller → handles HTTP request/response
Service    → handles business logic
Repository → handles DB operations

Example:
YouTubeController → receives HTTP request
YouTubeService    → fetches and saves videos
VideoRepository   → saves to DB
```

**Why Service Layer:**
```
✅ Separation of concerns
✅ Business logic reusable
✅ Easy to test independently
✅ Controllers stay thin and clean
✅ Services can call other services
```

**Problems without it:**
```
❌ Business logic in controller
❌ Controllers become huge and messy
❌ Logic not reusable
❌ Hard to test
```

---

### 12.2 Repository Pattern

**What:**
```
Separate data access logic into Repository classes
Services don't know HOW data is stored
Just ask repository for data
```

**How we implemented:**
```kotlin
interface VideoRepository : JpaRepository<Video, Long> {
    fun existsByVideoId(videoId: String): Boolean
    fun findByCategoryIn(
        categories: List<VideoCategory>,
        pageable: Pageable
    ): List<Video>
}
```

**Why Repository Pattern:**
```
✅ Data access logic in one place
✅ Easy to switch DB (MySQL → PostgreSQL)
✅ Easy to mock in tests
✅ Clean separation from business logic
✅ Spring Data JPA handles implementation
```

**Problems without it:**
```
❌ DB queries scattered everywhere
❌ Hard to change DB later
❌ Duplicate queries in multiple places
❌ Hard to test
```

---

### 12.3 Filter Pattern

**What:**
```
Chain of filters that process requests
Each filter does one specific job
Passes to next filter when done
```

**Our filter chain:**
```
Request
→ RateLimitFilter    → check rate limit
→ JwtAuthFilter      → verify JWT token
→ Spring Security    → check permissions
→ Controller         → handle request
→ Response
```

**Why Filter Pattern:**
```
✅ Each filter has single responsibility
✅ Easy to add new filters
✅ Easy to remove filters
✅ Order controlled
✅ Applied to ALL requests automatically
```

**Problems without it:**
```
❌ Rate limit check in every controller ❌
❌ JWT check in every controller ❌
❌ Code duplication everywhere ❌
❌ Easy to forget one endpoint ❌
```

---

### 12.4 Fallback Pattern

**What:**
```
When primary operation fails
→ return safe default value
→ instead of crashing
```

**How we implemented:**
```kotlin
@CircuitBreaker(name = "youtubeService",
                fallbackMethod = "fallbackF1Videos")
fun fetchAndStoreF1Videos(): List<Video> {
    // primary operation
}

fun fallbackF1Videos(e: Exception): List<Video> {
    logger.error("⚡ Circuit OPEN! Returning empty.")
    return emptyList()  // safe default ✅
}
```

**Fallbacks in our project:**
```
YouTube fails    → return []
F1 API fails     → return []
Cricket fails    → return []
Gemini fails     → return error message
RSS fails        → skip that source
```

**Why Fallback Pattern:**
```
✅ App never crashes on external failure
✅ Graceful degradation
✅ User gets response even if degraded
✅ Other features still work
```

---

### 12.5 Scheduler Pattern

**What:**
```
Run tasks automatically at fixed intervals
Without user triggering them
```

**How we implemented:**
```kotlin
@Scheduled(fixedRate = 600000) // every 10 minutes
fun fetchVideos() {
    youTubeService.fetchAndStoreF1Videos()
    youTubeService.fetchAndStoreICCVideos()
    rssFeedService.fetchAndStoreF1Articles()
    rssFeedService.fetchAndStoreCricketArticles()
    feedService.clearFeedCache()
}
```

**Why Scheduler Pattern:**
```
✅ Data always fresh without user action
✅ API quota managed efficiently
✅ Users never wait for fetch
✅ Background processing
```

**Problems without it:**
```
❌ User triggers fetch → waits 3-4 seconds ❌
❌ YouTube API called on every user request ❌
❌ Quota exhausted quickly ❌
```

---

### 12.6 Producer Consumer Pattern

**What:**
```
Producer creates messages
Consumer processes messages
Both work independently
```

**How we implemented:**
```
Producer:
YouTubeService saves video
→ VideoEventProducer sends to Kafka ✅

Consumer:
VideoEventConsumer listens to Kafka
→ Processes new video event ✅
```

**Why Producer Consumer:**
```
✅ Decoupled → independent scaling
✅ Async → fast response to user
✅ Buffer → consumer works at own pace
✅ Reliable → messages not lost
```

---

### 12.7 Builder Pattern

**What:**
```
Build complex objects step by step
```

**How we used it:**
```kotlin
// WebClient builder
WebClient.builder()
    .baseUrl("https://api.jolpi.ca/ergast/f1")
    .build()

// RetryTemplate builder
RetryTemplate().apply {
    setRetryPolicy(SimpleRetryPolicy(3))
    setBackOffPolicy(ExponentialBackOffPolicy())
}
```

**Why Builder Pattern:**
```
✅ Complex objects built step by step
✅ Readable and clear
✅ Optional parameters handled cleanly
✅ Immutable objects created
```

---

### 12.8 Singleton Pattern

**What:**
```
Only one instance of a class exists
Shared across entire application
```

**How Spring uses it:**
```
All @Service, @Repository, @Controller beans
→ Created once by Spring
→ Shared across all requests
→ Singleton by default ✅
```

**Examples in our project:**
```
YouTubeService    → one instance ✅
VideoRepository   → one instance ✅
RateLimitFilter   → one instance ✅
ChatService       → one instance ✅
```

**Why Singleton:**
```
✅ Memory efficient
✅ Shared state (like WebClient)
✅ Spring manages lifecycle
✅ No need to create objects manually
```

---

### 12.9 Data Transfer Object (DTO) Pattern

**What:**
```
Separate objects for transferring data
between layers
```

**How we used it:**
```kotlin
// Request DTO
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

// Response DTO
data class AuthResponse(
    val token: String
)

// Chat DTOs
data class ChatRequest(val question: String)
data class ChatResponse(val answer: String, val question: String)
```

**Why DTO Pattern:**
```
✅ Don't expose DB entities directly
✅ Control what data is sent/received
✅ Validation on request objects
✅ Clean API contracts
```

**Problems without DTOs:**
```
❌ Expose internal DB structure
❌ Password field exposed in response
❌ No validation possible
❌ Tight coupling between API and DB
```

---

## Summary Table

| Pattern | Problem Solved | Where Used |
|---------|---------------|------------|
| Service Layer | Separation of concerns | All services |
| Repository | Data access abstraction | All repositories |
| Filter | Cross cutting concerns | JWT, Rate limit |
| Fallback | Graceful degradation | Circuit Breaker |
| Scheduler | Background processing | VideoScheduler |
| Producer Consumer | Async decoupled processing | Kafka |
| Builder | Complex object creation | WebClient, RetryTemplate |
| Singleton | Single shared instance | All Spring beans |
| DTO | Clean data transfer | Requests/Responses |

---

## How Design Patterns Improved Our Project

```
Before patterns:
→ Business logic in controllers ❌
→ DB queries scattered everywhere ❌
→ Security checks in every controller ❌
→ Tight coupling between components ❌

After patterns:
→ Clean separation of concerns ✅
→ Data access in repositories only ✅
→ Security in filter chain ✅
→ Loosely coupled via Kafka ✅
→ Any developer can understand code instantly ✅
```

---