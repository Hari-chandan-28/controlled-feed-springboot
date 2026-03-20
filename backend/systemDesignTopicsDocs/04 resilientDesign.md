## 4. Resilience Strategy

---

## What is Resilience?

Resilience is the ability of your system to **handle failures gracefully and recover automatically** without crashing.

```
Without Resilience:
YouTube API fails → your app crashes ❌

With Resilience:
YouTube API fails → retry → circuit opens
→ fallback returns empty list
→ app keeps running ✅
```

---

## Simple Analogy

```
Without Resilience:
Power cut in your house
→ Everything stops working ❌

With Resilience:
Power cut in your house
→ Generator kicks in automatically
→ Everything keeps working ✅
```

---

## Subtopics We Used

---

### 4.1 Retry Mechanism

**What:**
```
When a request fails → automatically try again
Instead of giving up immediately
```

**How we implemented:**
```kotlin
RetryTemplate().apply {
    setRetryPolicy(SimpleRetryPolicy(3))
    setBackOffPolicy(ExponentialBackOffPolicy().apply {
        initialInterval = 2000  // 2 seconds
        multiplier = 2.0        // doubles each time
    })
}
```

**Retry flow:**
```
Request fails
→ Wait 2 seconds → retry 1
→ Still fails
→ Wait 4 seconds → retry 2
→ Still fails
→ Wait 8 seconds → retry 3
→ Still fails → give up → throw exception
```

**Why Exponential Backoff:**
```
Fixed retry (every 1 second):
→ API is overloaded
→ You hammer it every second
→ Makes it worse ❌

Exponential backoff:
→ API is overloaded
→ You wait longer each time
→ Gives API time to recover ✅
```

**Why Retry:**
```
✅ Handles temporary failures
✅ Network blips → recovered automatically
✅ API rate limit → wait and retry
✅ No user intervention needed
```

**Problems without Retry:**
```
❌ One network blip → operation fails permanently
❌ User has to manually retry
❌ Temporary failures look like permanent failures
```

**When NOT to retry:**
```
❌ 400 Bad Request → wrong data, retry won't help
❌ 401 Unauthorized → wrong key, retry won't help
❌ 404 Not Found → resource missing, retry won't help
✅ 500 Server Error → retry makes sense
✅ 503 Service Unavailable → retry makes sense
✅ Network timeout → retry makes sense
```

**How to improve:**
```
→ Only retry on specific exceptions
→ Add jitter (random delay) to avoid thundering herd
→ Set max retry duration not just count
→ Log each retry attempt
```

---

### 4.2 Circuit Breaker

**What:**
```
Monitors failures over time
When too many failures → opens circuit
→ Stops calling failing service
→ Returns fallback immediately
→ After cooldown → tests service again
```

**3 States:**
```
CLOSED    → everything normal ✅
OPEN      → too many failures, stop calling ⛔
HALF-OPEN → testing if service recovered 🟡
```

**How we configured:**
```properties
sliding-window-size=5
failure-rate-threshold=50
wait-duration-in-open-state=30s
permitted-number-of-calls-in-half-open-state=2
```

**What this means:**
```
Watch last 5 requests
If 50% fail → open circuit
Wait 30 seconds
Allow 2 test requests
Both succeed → close circuit ✅
Any fail → open again ⛔
```

**Why Circuit Breaker:**
```
Without Circuit Breaker:
1000 users × failing API × 3 retries
= 3000 wasted requests ❌
= Server overwhelmed ❌
= All users wait 6 seconds ❌

With Circuit Breaker:
2 failures → circuit opens
998 users → instant fallback response ✅
= Server protected ✅
= Users get fast response ✅
```

**Fallback methods:**
```kotlin
fun fallbackF1Videos(e: Exception): List<Video> {
    logger.error("⚡ Circuit OPEN! Returning empty.")
    return emptyList()
}
```

**Problems without Circuit Breaker:**
```
❌ Cascade failures → one service down → all down
❌ Resources wasted on failing requests
❌ Users wait forever
❌ Server runs out of threads
```

**How to improve:**
```
→ Return cached data as fallback instead of empty
→ Add circuit breaker metrics dashboard
→ Different thresholds per service
→ Alert when circuit opens
```

---

### 4.3 Retry vs Circuit Breaker Together

**How they work together:**
```
Request comes in
→ Circuit Breaker checks → CLOSED → allow
   → Retry attempts 1,2,3 → all fail
   → Exception thrown to Circuit Breaker
   → Circuit Breaker counts failure
   → After threshold → OPEN
   
Next request comes in
→ Circuit Breaker checks → OPEN
   → Return fallback immediately
   → No retry needed ✅
```

**Why both together:**
```
Retry alone:
→ Every request retries 3 times
→ 1000 users × 3 retries = 3000 calls ❌

Circuit Breaker alone:
→ Opens after first failure
→ No chance for temporary issues to recover ❌

Both together:
→ Retry handles temporary failures ✅
→ Circuit Breaker handles persistent failures ✅
→ Best of both worlds ✅
```

---

### 4.4 Health Check

**What:**
```
Endpoint that reports if app and its 
dependencies are healthy
```

**How we implemented:**
```
GET /actuator/health

Response:
{
    "status": "UP",
    "components": {
        "db": { "status": "UP" },
        "redis": { "status": "UP" },
        "diskSpace": { "status": "UP" }
    }
}
```

**Why Health Check:**
```
✅ Cloud platforms auto-restart if DOWN
✅ Load balancer removes unhealthy instances
✅ Early warning before users affected
✅ Monitor all dependencies in one call
```

**Problems without Health Check:**
```
❌ App crashes → nobody knows
❌ DB goes down → users get errors for hours
❌ Cloud can't auto-restart
❌ Manual monitoring required
```

**How to improve:**
```
→ Add custom health indicators
→ Check Kafka connection health
→ Check external API health
→ Add health check to CI/CD pipeline
```

---

### 4.5 Fallback Strategy

**What:**
```
When primary operation fails
→ return safe default value
→ instead of crashing
```

**Fallbacks in our project:**
```
YouTube API fails    → return empty list []
F1 standings fails   → return empty list []
Cricket API fails    → return empty list []
Gemini API fails     → return error message
```

**Why Fallback:**
```
✅ App never crashes on external API failure
✅ User gets response even if degraded
✅ Graceful degradation
```

**How to improve:**
```
→ Return cached data as fallback
→ Return partial data instead of empty
→ Show user friendly message
→ Queue failed requests for later retry
```

---

## Summary Table

| Pattern | Problem Solved | Tech Used |
|---------|---------------|-----------|
| Retry | Temporary failures | RetryTemplate |
| Exponential Backoff | API overload | ExponentialBackOffPolicy |
| Circuit Breaker | Persistent failures | Resilience4j |
| Health Check | Monitoring | Spring Actuator |
| Fallback | Graceful degradation | fallback methods |

---

## How Resilience Improved Our Project

```
Before Resilience:
→ YouTube API blip → all video fetches fail ❌
→ Ergast API down → F1 standings crash app ❌
→ 1000 users → 3000 wasted retry calls ❌

After Resilience:
→ YouTube API blip → retry recovers it ✅
→ Ergast API down → circuit opens → empty list ✅
→ 1000 users → 2 failures open circuit → fast fallback ✅
→ App never crashes on external failures ✅
```

---