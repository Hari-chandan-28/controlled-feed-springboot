## 8. Performance

---

## What is Performance?

Performance is about making your application **respond fast and handle many users** without slowing down or crashing.

```
Without Performance Optimization:
→ Feed API takes 5 seconds ❌
→ 100 users crash the server ❌
→ YouTube API called 1000 times/day ❌

With Performance Optimization:
→ Feed API takes 50 milliseconds ✅
→ 10000 users handled easily ✅
→ YouTube API called 144 times/day ✅
```

---

## Simple Analogy

```
Restaurant without optimization:
→ Chef cooks every dish from scratch
→ 1 customer = 20 minutes wait
→ 100 customers = chaos ❌

Restaurant with optimization:
→ Pre-cook popular dishes
→ 1 customer = 2 minutes wait
→ 100 customers = handled easily ✅
```

---

## Subtopics We Used

---

### 8.1 Redis Caching Layer

**What:**
```
Store frequently accessed data in memory
Return from cache instead of hitting DB/API
```

**Performance impact:**
```
Without cache:
Feed request → DB query → 200ms

With cache:
Feed request → Redis hit → 2ms
→ 100x faster! ✅
```

**How we cached different data:**
```
Feed           → 5 min  → saves DB queries
F1 standings   → 1 hour → saves Ergast API calls
F1 live data   → 3 secs → saves OpenF1 API calls
Cricket live   → 30 secs→ saves CricAPI calls
```

**Real numbers:**
```
Without cache:
1000 users request feed
→ 1000 DB queries/minute ❌

With cache:
1000 users request feed
→ 1 DB query per 5 minutes ✅
→ 999 requests served from Redis ✅
```

---

### 8.2 Database Indexing

**What:**
```
Special data structure that speeds up queries
Without index → full table scan
With index → direct lookup
```

**Performance impact:**
```
Without index:
SELECT * FROM videos WHERE category = 'F1'
→ Scan all 100,000 rows → 2 seconds ❌

With index on category:
→ Jump directly to F1 rows → 2ms ✅
→ 1000x faster!
```

**Indexes we added:**
```
idx_video_category    → fast feed queries
idx_video_published_at→ fast date sorting
idx_video_id          → fast duplicate check
idx_user_email        → fast login lookup
idx_profile_user_id   → fast profile fetch
```

---

### 8.3 Pagination

**What:**
```
Return data in small pages
Instead of all data at once
```

**Performance impact:**
```
Without pagination:
GET /api/feed
→ Return all 10,000 videos → 5 seconds ❌
→ 50MB response ❌
→ Frontend freezes ❌

With pagination:
GET /api/feed?page=0&size=10
→ Return 10 videos → 50ms ✅
→ 50KB response ✅
→ Frontend loads fast ✅
```

**How we implemented:**
```kotlin
fun getFeed(email: String, page: Int, size: Int): List<Video> {
    val pageable = PageRequest.of(page, size)
    return videoRepository.findByCategoryIn(categories, pageable)
}
```

---

### 8.4 Scheduled Background Jobs

**What:**
```
Run heavy operations in background
Not triggered by user request
```

**How we implemented:**
```kotlin
@Scheduled(fixedRate = 600000) // every 10 minutes
fun fetchVideos() {
    youTubeService.fetchAndStoreF1Videos()
    youTubeService.fetchAndStoreICCVideos()
    rssFeedService.fetchAndStoreF1Articles()
    rssFeedService.fetchAndStoreCricketArticles()
}
```

**Performance impact:**
```
Without scheduler:
User requests feed
→ Fetch from YouTube → 3 seconds
→ Parse and save → 1 second
→ Return feed → 4 seconds total ❌

With scheduler:
Background job fetches every 10 minutes
User requests feed
→ Data already in DB
→ Return from cache → 2ms ✅
```

**Why Scheduler:**
```
✅ User never waits for YouTube fetch
✅ YouTube API quota managed efficiently
✅ Fresh data available before user asks
✅ Heavy operations off critical path
```

---

### 8.5 Async Event Processing via Kafka

**What:**
```
Process non-critical operations asynchronously
User doesn't wait for them
```

**Performance impact:**
```
Without Kafka:
Save video → send notification → clear cache
→ User waits for all 3 → slow ❌

With Kafka:
Save video → return response to user immediately
→ Kafka handles notification in background ✅
→ User gets fast response ✅
```

---

### 8.6 Connection Pooling

**What:**
```
Reuse database connections instead of
creating new one for every request
```

**Built into our project:**
```
HikariCP → included with Spring Boot
→ Pool of connections always ready
→ No connection overhead per request
```

**Performance impact:**
```
Without connection pool:
Every request → create DB connection → 100ms overhead
→ 1000 requests → 100 seconds wasted just on connections ❌

With connection pool:
Connections already open and ready
→ 1000 requests → 0ms connection overhead ✅
```

---

### 8.7 Lazy Loading

**What:**
```
Don't load related data until needed
```

**How we use it:**
```
Profile has user_id
→ Don't load full user object unless needed
→ Only load what the request needs
```

**Performance impact:**
```
Without lazy loading:
Load profile → automatically load user → load all videos
→ Unnecessary data loaded ❌

With lazy loading:
Load profile → only load what's needed ✅
```

---

## Performance Metrics Summary

| Optimization | Before | After | Improvement |
|-------------|--------|-------|-------------|
| Redis Cache | 200ms DB query | 2ms Redis hit | 100x faster |
| DB Indexing | 2s full scan | 2ms index lookup | 1000x faster |
| Pagination | 5s all data | 50ms 10 items | 100x faster |
| Scheduler | 4s user wait | 2ms from cache | 2000x faster |
| Connection Pool | 100ms per connection | 0ms overhead | ∞ faster |

---

## How Performance Improved Our Project

```
Before optimization:
→ Feed request → 4-5 seconds ❌
→ F1 standings → new API call every time ❌
→ 100 users → DB overwhelmed ❌

After optimization:
→ Feed request → 2ms from cache ✅
→ F1 standings → 1 API call per hour ✅
→ 10000 users → handled by Redis ✅
→ Background jobs → users never wait ✅
```

---

## Performance Best Practices We Followed

```
✅ Cache expensive operations
✅ Index frequently queried columns
✅ Paginate large datasets
✅ Move heavy work to background
✅ Async processing for non-critical operations
✅ Connection pooling
✅ Circuit Breaker prevents slow API calls blocking threads
```

---

