## `architecture.md` — What to Include

```
1. Overview
2. Architecture Diagram
3. Components and their roles
4. Request flow
5. Data flow
6. Technology mapping
7. External integrations
```

---

## Let's Start Section by Section

---

## Section 1 — Overview

Architecture overview answers:
```
What type of architecture is this?
How many layers does it have?
How do components connect?
```

Our architecture:
```
Type: Monolithic REST API
Layers: 3 tier (Controller → Service → Repository)
Pattern: Layered Architecture + Event Driven
```

---

## Section 2 — Architecture Diagram

```
                        ┌─────────────────┐
                        │     CLIENT      │
                        │  (Postman/App)  │
                        └────────┬────────┘
                                 │ HTTP Request
                                 ▼
                        ┌─────────────────┐
                        │   RATE LIMIT    │
                        │    FILTER       │
                        │   (Bucket4j)    │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │   JWT AUTH      │
                        │    FILTER       │
                        │ (Spring Security)│
                        └────────┬────────┘
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │      CONTROLLERS       │
                    ├────────────────────────┤
                    │ Auth   │ Profile       │
                    │ Feed   │ YouTube       │
                    │ RSS    │ F1Dashboard   │
                    │ Cricket│ Chat          │
                    └────────┬───────────────┘
                             │
                             ▼
                    ┌────────────────────────┐
                    │       SERVICES         │
                    ├────────────────────────┤
                    │ AuthService            │
                    │ ProfileService         │
                    │ YouTubeService         │
                    │ FeedService            │
                    │ RssFeedService         │
                    │ F1DashboardService     │
                    │ CricketService         │
                    │ ChatService            │
                    │ VideoEventProducer     │
                    │ VideoEventConsumer     │
                    └────────┬───────────────┘
                             │
               ┌─────────────┼─────────────┐
               │             │             │
               ▼             ▼             ▼
    ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
    │    MYSQL     │ │    REDIS     │ │    KAFKA     │
    │  (Primary    │ │  (Cache)     │ │  (Events)    │
    │   Storage)   │ │             │ │              │
    └──────────────┘ └──────────────┘ └──────────────┘
               │
               ▼
    ┌──────────────────────────────────────┐
    │         EXTERNAL APIs                │
    ├──────────────────────────────────────┤
    │ YouTube API  │ Ergast/Jolpi API      │
    │ OpenF1 API   │ CricAPI               │
    │ RSS Feeds    │ Gemini AI API         │
    └──────────────────────────────────────┘
```

---

## Section 3 — Components and Their Roles

| Component | Technology | Role |
|-----------|-----------|------|
| Rate Limit Filter | Bucket4j | Limit requests per IP |
| JWT Auth Filter | Spring Security | Verify JWT token |
| Controllers | Spring MVC | Handle HTTP requests |
| Services | Spring | Business logic |
| Repositories | Spring Data JPA | Data access |
| MySQL | MySQL 8.x | Primary data storage |
| Redis | Redis 5.x | Caching layer |
| Kafka | Apache Kafka 3.7 | Event streaming |
| Scheduler | Spring Scheduler | Background jobs |
| Circuit Breaker | Resilience4j | Failure protection |
| Retry | Spring Retry | Temporary failure recovery |

---

## Section 4 — Request Flow

### Normal API Request
```
1. Client sends request with JWT token
2. Rate Limit Filter → check IP limit
3. JWT Auth Filter → verify token
4. Controller → receive request
5. Service → business logic
6. Redis → check cache
   → Cache HIT → return cached data
   → Cache MISS → continue
7. Repository → query MySQL
8. Cache result in Redis
9. Return response to client
```

### YouTube Video Fetch Flow
```
1. Scheduler triggers every 10 minutes
2. YouTubeService → calls YouTube API
   → Retry on failure (3 attempts)
   → Circuit Breaker protection
3. Parse response → save to MySQL
4. VideoEventProducer → send to Kafka
5. VideoEventConsumer → process event
6. FeedService → clear Redis cache
```

### Feed Request Flow
```
1. Client → GET /api/feed?page=0&size=10
2. JWT verified → get user email
3. FeedService → get user profile
4. Get user genres (F1/CRICKET)
5. Redis → check cache
   → HIT → return cached feed
   → MISS → query MySQL by category
6. Cache result → return to client
```

---

## Section 5 — Data Flow

```
External APIs → Services → MySQL (permanent storage)
                        → Redis (temporary cache)
                        → Kafka (events)

User Request → Redis (if cached)
             → MySQL (if not cached) → Redis (store)
             → Response to user
```

---

## Section 6 — Technology Mapping

| Layer | Technology | Purpose |
|-------|-----------|---------|
| API Layer | Spring MVC | REST endpoints |
| Security | Spring Security + JWT | Auth and authorization |
| Business Logic | Kotlin Services | Core application logic |
| Caching | Redis | Fast data access |
| Persistence | MySQL + JPA | Permanent storage |
| Messaging | Apache Kafka | Async event processing |
| Resilience | Resilience4j + Spring Retry | Fault tolerance |
| Scheduling | Spring Scheduler | Background jobs |
| AI | Google Gemini API | Sports chatbot |
| Documentation | Springdoc OpenAPI | Swagger UI |
| Monitoring | Spring Actuator | Health checks |

---

## Section 7 — External Integrations

```
┌─────────────────────────────────────────────┐
│           EXTERNAL INTEGRATIONS              │
├──────────────┬──────────────────────────────┤
│ YouTube API  │ F1 + Cricket videos           │
│              │ Auth: API Key                 │
│              │ Limit: 100 searches/day       │
├──────────────┼──────────────────────────────┤
│ Ergast API   │ F1 season data                │
│              │ Auth: None                    │
│              │ Limit: Unlimited              │
├──────────────┼──────────────────────────────┤
│ OpenF1 API   │ F1 live race data             │
│              │ Auth: None                    │
│              │ Limit: Unlimited              │
├──────────────┼──────────────────────────────┤
│ CricAPI      │ Cricket live scores           │
│              │ Auth: API Key                 │
│              │ Limit: 100 calls/day          │
├──────────────┼──────────────────────────────┤
│ RSS Feeds    │ F1 + Cricket news             │
│              │ Auth: None                    │
│              │ Sources: 8 total              │
├──────────────┼──────────────────────────────┤
│ Gemini API   │ AI sports chatbot             │
│              │ Auth: API Key                 │
│              │ Limit: Free tier              │
└──────────────┴──────────────────────────────┘
```

---

## Section 8 — Database Schema Overview

```
┌──────────┐      ┌──────────────┐
│  users   │─────▶│   profiles   │
│          │ 1:1  │              │
│ id       │      │ id           │
│ name     │      │ user_id (FK) │
│ email    │      │ bio          │
│ password │      │ genres       │
│ role     │      │ picture      │
└──────────┘      └──────────────┘

┌──────────────┐      ┌──────────────┐
│   videos     │      │   articles   │
│              │      │              │
│ id           │      │ id           │
│ video_id     │      │ guid         │
│ title        │      │ title        │
│ description  │      │ description  │
│ thumbnail    │      │ link         │
│ published_at │      │ image_url    │
│ channel      │      │ published_at │
│ category     │      │ source       │
└──────────────┘      │ category     │
                      └──────────────┘
```

---

## Section 9 — Resilience Architecture

```
External API Call
       │
       ▼
┌─────────────────┐
│ Circuit Breaker │ ← monitors failures
│   (Resilience4j)│
└────────┬────────┘
         │ CLOSED (normal)
         ▼
┌─────────────────┐
│  Retry Template │ ← retries on failure
│  3 attempts     │
│  2s → 4s → 8s  │
└────────┬────────┘
         │ all retries failed
         ▼
┌─────────────────┐
│ Circuit OPEN    │ ← stop calling API
│ 30s cooldown    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Fallback Method │ ← return safe default
│ return []       │
└─────────────────┘
```

---