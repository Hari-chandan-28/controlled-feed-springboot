## 3. Caching Strategy

---

## What is Caching?

Caching is **storing frequently accessed data in fast memory** so you don't need to fetch it again from slow sources.

```
Without Cache:
Request → DB query every time → slow ❌

With Cache:
Request 1 → DB query → store in Redis
Request 2 → Redis hit → no DB query → fast ✅
```

---

## Simple Analogy

```
Without cache:
Every time you want to know the capital of France
→ Go to library → find book → read answer
→ Takes 10 minutes every time ❌

With cache:
First time → go to library → remember answer
Next time → answer already in your head
→ Takes 1 second ✅
```

---

## Subtopics We Used

---

### 3.1 Redis

**What:**
```
Redis = Remote Dictionary Server
In-memory key-value store
Extremely fast (microsecond responses)
```

**Why Redis:**
```
✅ 100x faster than DB queries
✅ Supports TTL (auto expiry)
✅ Industry standard cache
✅ Works perfectly with Spring Boot
✅ Supports complex data structures
```

**Problems without Redis:**
```
❌ Every feed request hits DB
❌ 1000 users = 1000 DB queries/minute
❌ DB gets overwhelmed
❌ Slow response times
```

---

### 3.2 TTL Based Cache Expiry

**What:**
```
TTL = Time To Live
Cache automatically expires after set time
```

**How we configured different TTLs:**
```
Feed data          → 5 minutes
  → Changes every scheduler run (10 mins)
  → 5 min cache is safe

F1 standings       → 1 hour
  → Only changes after race weekend
  → 1 hour cache is safe

F1 live positions  → 3 seconds
  → Changes every 3-4 seconds during race
  → Must be fresh

Cricket live       → 30 seconds
  → Changes frequently during match
  → 30 sec is balance of freshness vs performance

Cricket upcoming   → 1 hour
  → Doesn't change often
  → 1 hour cache is safe
```

**Why different TTLs:**
```
✅ Fresh data where needed (live timing)
✅ Reduced DB/API load for slow changing data
✅ Balance between performance and freshness
```

**Problems with wrong TTL:**
```
Too long TTL:
❌ Stale data shown to users
❌ Live scores show wrong info

Too short TTL:
❌ Cache never actually helps
❌ DB still gets hammered
```

---

### 3.3 Cache Invalidation

**What:**
```
Clearing cache when data changes
So users don't see stale data
```

**How we implemented:**
```kotlin
// When new videos fetched → clear feed cache
@CacheEvict(value = ["feed"], allEntries = true)
fun clearFeedCache()

// Called in VideoScheduler after fetching new videos
→ New videos available → old feed cache cleared
→ Next request → fresh feed from DB → cached again
```

**Why Cache Invalidation:**
```
✅ Users always see latest videos
✅ Cache stays consistent with DB
✅ Automatic cleanup
```

**Problems without invalidation:**
```
❌ New videos fetched but users see old feed
❌ Cache shows deleted data
❌ Data inconsistency between cache and DB
```

**How to improve:**
```
→ Event driven invalidation via Kafka
→ Selective invalidation (only affected users)
→ Cache versioning
```

---

### 3.4 @Cacheable Annotation

**What:**
```
Spring annotation that automatically:
→ Checks cache before method runs
→ Returns cached value if found
→ Runs method and caches result if not found
```

**How we used it:**
```kotlin
@Cacheable(value = ["f1-standings"], key = "'standings'")
fun getDriverStandings(): List<DriverStanding> {
    // Only runs if cache miss!
    return ergastClient.get()...
}
```

**Flow:**
```
Request comes in
→ Check Redis for key "f1-standings::standings"
→ Found? Return cached value instantly ✅
→ Not found? Run method → cache result → return
```

**Why @Cacheable:**
```
✅ No code change needed in method
✅ Automatic cache management
✅ Clean and readable code
✅ Works with any return type
```

---

### 3.5 Cache Keys

**What:**
```
Unique identifier for each cached value
Wrong key = wrong cache hit!
```

**How we designed keys:**
```
Feed cache:
key = email + page + size
→ "hari@gmail.com-0-10"
→ Different cache per user per page ✅

F1 standings:
key = 'standings'
→ Same for all users ✅
→ F1 standings same for everyone

Cricket scorecard:
key = matchId
→ "abc123"
→ Different cache per match ✅
```

**Why good cache keys:**
```
✅ User A gets their own feed
✅ User B gets their own feed
✅ F1 standings shared → saves memory
✅ Each match has own scorecard cache
```

**Problems with wrong keys:**
```
❌ User A sees User B's feed
❌ All users get same scorecard
❌ Cache always missed → never helps
```

---

## Summary Table

| Data | Cache TTL | Why |
|------|-----------|-----|
| Feed | 5 minutes | Refreshed by scheduler |
| F1 Standings | 1 hour | Changes rarely |
| F1 Results | 1 hour | Historical data |
| F1 Schedule | 1 hour | Fixed calendar |
| F1 Live Positions | 3 seconds | Updates every 3-4 secs |
| F1 Live Timing | 3 seconds | Updates every lap |
| F1 Live Intervals | 3 seconds | Updates every 3-4 secs |
| Cricket Live | 30 seconds | Updates during match |
| Cricket Upcoming | 1 hour | Fixed schedule |
| Cricket Scorecard | 30 seconds | Updates during match |

---

## How Caching Improved Our Project

```
Before caching:
→ Every feed request → DB query
→ Every F1 standings → Ergast API call
→ 1000 users = 1000 API calls/minute
→ Ergast API rate limited ❌

After caching:
→ Feed → Redis hit after first request ✅
→ F1 standings → 1 API call per hour ✅
→ 1000 users = 1 API call per hour ✅
→ Response time: seconds → milliseconds ✅
```

---

## Cache Hit vs Miss

```
Cache HIT  → data found in Redis → instant response ⚡
Cache MISS → data not in Redis → fetch from DB/API → cache it
```

---
