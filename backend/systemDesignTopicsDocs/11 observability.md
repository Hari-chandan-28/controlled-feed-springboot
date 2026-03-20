## 11. Observability

---

## What is Observability?

Observability is the ability to **understand what is happening inside your system** from the outside.

```
Without Observability:
App is slow → you have no idea why ❌
Something fails → you find out from users ❌
DB is down → you discover after 2 hours ❌

With Observability:
App is slow → logs show exactly which query ✅
Something fails → alert sent immediately ✅
DB is down → health check catches it instantly ✅
```

---

## Simple Analogy

```
Car without dashboard:
→ No speedometer
→ No fuel gauge
→ No warning lights
→ Engine fails → total surprise ❌

Car with dashboard:
→ See speed, fuel, temperature
→ Warning light before breakdown
→ Take action before failure ✅
```

---

## 3 Pillars of Observability

```
1. Logs     → what happened
2. Metrics  → how much/how fast
3. Traces   → where time was spent
```

---

## Subtopics We Used

---

### 11.1 Structured Logging (SLF4J)

**What:**
```
Recording important events as they happen
With timestamp, level and message
```

**How we implemented:**
```kotlin
private val logger = LoggerFactory.getLogger(YouTubeService::class.java)

logger.info("🔄 Attempting to fetch F1 videos...")
logger.info("✅ F1 YouTube Response received!")
logger.error("❌ All retries failed for F1! Error: ${e.message}")
logger.warn("⚡ Circuit OPEN for F1!")
```

**Log levels:**
```
DEBUG → detailed technical info (dev only)
INFO  → normal operations ✅
WARN  → something unexpected but not critical
ERROR → something failed ❌
```

**Log format:**
```
2026-03-09T10:50:04 INFO  YouTubeService : 🔄 Attempting to fetch F1 videos...
2026-03-09T10:50:16 INFO  YouTubeService : 🔄 Attempting to fetch F1 videos... (retry 2)
2026-03-09T10:50:20 ERROR YouTubeService : ❌ All retries failed for F1!
```

**Why Structured Logging:**
```
✅ Track what happened when
✅ Debug issues quickly
✅ Understand retry behavior
✅ Monitor circuit breaker state
✅ Essential for production support
```

**Problems without logging:**
```
❌ Something fails → no idea what happened
❌ Debugging takes hours
❌ Can't reproduce issues
❌ No audit trail
```

**How to improve:**
```
→ Add correlation/request ID to every log
→ Add user email to logs (for debugging)
→ Structured JSON logging for log aggregation
→ Log to file not just console
→ Log rotation to manage disk space
```

---

### 11.2 Health Check (Spring Actuator)

**What:**
```
Endpoint that reports status of app
and all its dependencies
```

**How we implemented:**
```properties
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
management.health.redis.enabled=true
management.health.db.enabled=true
```

**Response:**
```json
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "MySQL"
            }
        },
        "redis": {
            "status": "UP",
            "details": {
                "version": "5.0.14"
            }
        },
        "diskSpace": {
            "status": "UP"
        }
    }
}
```

**Status meanings:**
```
UP      → component working fine ✅
DOWN    → component failed ❌
UNKNOWN → can't determine status ⚠️
```

**Why Health Check:**
```
✅ Cloud platforms auto-restart on DOWN
✅ Load balancer removes unhealthy instances
✅ Monitor all dependencies in one call
✅ Early warning system
✅ Zero manual monitoring needed
```

**Problems without Health Check:**
```
❌ DB goes down → users get errors for hours
❌ Nobody knows until users complain
❌ Cloud can't auto-restart
❌ Manual monitoring required 24/7
```

---

### 11.3 Circuit Breaker State Monitoring

**What:**
```
Track state of Circuit Breakers
Know when they open and close
```

**What we log:**
```
Circuit CLOSED → normal operation
⚡ Circuit OPEN → service is down
🟡 Circuit HALF-OPEN → testing recovery
```

**From our logs:**
```
INFO  : 🔄 Fetching F1 standings...
ERROR : ⚡ Circuit OPEN for F1 standings! Error: timeout
INFO  : 🔄 Fetching F1 standings... (half-open test)
INFO  : ✅ F1 standings received! Circuit CLOSED
```

**Why Circuit Breaker Monitoring:**
```
✅ Know which external APIs are failing
✅ Know how often circuit opens
✅ Identify unreliable external services
✅ Plan improvements based on data
```

---

### 11.4 Actuator Endpoints

**What:**
```
Spring Boot Actuator provides built in
monitoring endpoints
```

**Endpoints available:**
```
/actuator/health  → app health ✅ (we enabled)
/actuator/info    → app info ✅ (we enabled)
/actuator/metrics → performance metrics
/actuator/env     → environment properties
/actuator/loggers → log levels
/actuator/beans   → all Spring beans
```

**Currently exposed:**
```properties
management.endpoints.web.exposure.include=health,info
```

**How to improve:**
```
→ Expose metrics endpoint
→ Connect to Prometheus + Grafana
→ Real time performance dashboard
→ Alert when metrics exceed thresholds
```

---

### 11.5 Error Logging

**What:**
```
Log all errors with enough context
to debug and fix them
```

**How we log errors:**
```kotlin
// Retry errors
logger.error("❌ F1 attempt $attempt failed: ${e.message}")

// Circuit Breaker
logger.error("⚡ Circuit OPEN for F1! Error: ${e.message}")

// Parsing errors
logger.error("Error parsing YouTube response: ${e.message}")

// API errors
logger.error("❌ Error Calling Gemini API: ${e.message}")
```

**Good error log includes:**
```
✅ What operation failed
✅ Error message
✅ Which retry attempt
✅ Which service/API
✅ Timestamp (automatic)
```

**How to improve:**
```
→ Add stack trace for unexpected errors
→ Add request ID for tracing
→ Add user context (email)
→ Send critical errors to Slack/email
→ Error rate alerting
```

---

### 11.6 What's Missing — Future Improvements

**Metrics (not implemented yet):**
```
→ Request count per endpoint
→ Response time per endpoint
→ Error rate per endpoint
→ Cache hit/miss ratio
→ DB query execution time

Tools:
→ Micrometer (built into Spring Boot)
→ Prometheus (metrics storage)
→ Grafana (metrics dashboard)
```

**Distributed Tracing (not implemented yet):**
```
→ Track request across all services
→ See exactly where time is spent
→ Identify bottlenecks

Tools:
→ Zipkin
→ Jaeger
→ Spring Cloud Sleuth
```

**Alerting (not implemented yet):**
```
→ Alert when circuit opens
→ Alert when error rate spikes
→ Alert when response time high
→ Alert when DB connection fails

Tools:
→ PagerDuty
→ Slack notifications
→ Email alerts
```

---

## Summary Table

| Feature | What We Monitor | Tool |
|---------|----------------|------|
| Logging | All operations | SLF4J |
| Health Check | DB, Redis, Disk | Spring Actuator |
| Circuit Breaker | External API state | Resilience4j logs |
| Error Logging | All failures | SLF4J |
| Metrics | Not implemented yet | Micrometer (future) |
| Tracing | Not implemented yet | Zipkin (future) |
| Alerting | Not implemented yet | PagerDuty (future) |

---

## How Observability Improved Our Project

```
Before Observability:
→ Something fails → no idea why ❌
→ DB down → find out from users ❌
→ Retry happening → can't see it ❌

After Observability:
→ Something fails → logs show exactly why ✅
→ DB down → health check catches it ✅
→ Retry happening → logs show each attempt ✅
→ Circuit opens → logged immediately ✅
```

---

## Observability in Our Logs — Real Example

```
10:49:00 INFO  Scheduler started
10:49:00 INFO  🔄 Attempting to fetch F1 videos...
10:49:00 INFO  Fetching F1 videos... (attempt 1)
10:49:02 ERROR ❌ F1 attempt 1 failed: 400 Bad Request
10:49:04 INFO  Fetching F1 videos... (attempt 2)
10:49:08 ERROR ❌ F1 attempt 2 failed: 400 Bad Request
10:49:08 ERROR ❌ All retries failed for F1!
10:49:08 ERROR ⚡ Circuit OPEN for F1!
10:49:08 INFO  Scheduler completed
```

From these logs we can tell:
```
✅ When scheduler ran
✅ How many retries happened
✅ What error occurred
✅ When circuit opened
✅ Total time taken
```

---