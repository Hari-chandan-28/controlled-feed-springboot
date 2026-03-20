### What is System Design?
    System Design is the process of planning how to build a system before writing code.
    It answers:
    What components do we need?
    How do they talk to each other?
    How do we handle failures?
    How do we scale?
    How do we store data?

### Simple Analogy
    Building a house:
    → You don't just start laying bricks
    → You first make a blueprint
    → Plan rooms, doors, electricity, plumbing
    
    Building software:
    → You don't just start coding
    → You first design the system
    → Plan APIs, database, caching, security
    
    What Makes Us Use It
    Without System Design:
    → Build features randomly
    → Performance issues discovered late
    → Hard to scale when users grow
    → One failure crashes everything
    → Security holes discovered in production
    
    With System Design:
    → Clear plan before coding
    → Performance considered upfront
    → Scale ready from day one
    → Failures handled gracefully
    → Security built in from start

### In Our Project — Somes Examples
    Problem: YouTube API can fail
    Design Decision: Add Retry + Circuit Breaker
    Result: App works even when YouTube is down ✅
    
    Problem: Feed API called 1000 times/minute
    Design Decision: Add Redis cache
    Result: Only 1 DB query per 5 minutes ✅
    
    Problem: Bad actors spamming signup
    Design Decision: Add Rate Limiting
    Result: Max 5 signup attempts per minute ✅
    
    Problem: Too many YouTube API calls
    Design Decision: Scheduler every 10 minutes
    Result: API quota never exceeded ✅
    
### Advantages
    1. Performance
       → Identify bottlenecks before they happen
       → Cache heavy queries
       → Index important columns
    
       2. Reliability
          → Handle failures gracefully
          → Retry failed requests
          → Circuit Breaker prevents cascade failures
    
       3. Scalability
          → Stateless APIs → easy to add more servers
          → Cache layer → reduce DB load
          → Kafka → decouple services
    
       4. Security
          → JWT → stateless auth
          → Rate limiting → prevent abuse
          → BCrypt → secure passwords
    
       5. Maintainability
          → Clear separation of concerns
          → Service layer pattern
          → Repository pattern

