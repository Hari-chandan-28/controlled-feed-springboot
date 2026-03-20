## 5. Messaging & Events (Kafka)

---

## What is Messaging?

Messaging is a way for different parts of your system to **communicate without directly calling each other**.

```
Without Messaging (Direct Call):
Service A → directly calls → Service B
→ If B is down → A fails too ❌

With Messaging (Kafka):
Service A → drops message in Kafka
→ Service B picks up when ready
→ If B is down → message waits ✅
```

---

## Simple Analogy

```
Without Kafka (Phone Call):
You call your friend
→ Friend busy → call fails ❌
→ You have to try again manually

With Kafka (WhatsApp Message):
You send message to friend
→ Friend busy → message waits
→ Friend reads when available ✅
→ You don't have to wait
```

---

## Subtopics We Used

---

### 5.1 Event Driven Architecture

**What:**
```
Instead of services calling each other directly
→ Services emit events
→ Other services react to events
```

**In our project:**
```
Without Events:
YouTubeService saves video
→ directly notifies feed service
→ directly clears cache
→ directly notifies users
→ All tightly coupled ❌

With Events:
YouTubeService saves video
→ emits "new-video" event to Kafka
→ Feed service listens → clears cache
→ Notification service listens → notifies users
→ All loosely coupled ✅
```

**Why Event Driven:**
```
✅ Services independent of each other
✅ Easy to add new consumers
✅ Failure in one doesn't affect others
✅ Async processing → faster response
```

**Problems without Events:**
```
❌ Tight coupling between services
❌ One service down → all fail
❌ Hard to add new features
❌ Synchronous → slower response
```

---

### 5.2 Kafka

**What:**
```
Apache Kafka is a distributed message queue
Acts as middle man between services
Stores messages reliably
Delivers to consumers in order
```

**Core concepts:**
```
Producer  → sends messages to Kafka
Topic     → category for messages (like "new-video")
Consumer  → reads messages from topic
Broker    → Kafka server that stores messages
Partition → topic split into parts for parallelism
Offset    → position of message in partition
```

**Why Kafka:**
```
✅ Handles millions of messages per second
✅ Messages stored reliably
✅ Multiple consumers can read same message
✅ Replay messages if something goes wrong
✅ Industry standard (Netflix, Uber, LinkedIn)
```

**Problems without Kafka:**
```
❌ Direct service calls → tight coupling
❌ Lost events if service is down
❌ Hard to scale
❌ No message replay
```

---

### 5.3 Producer

**What:**
```
Component that sends messages to Kafka topic
```

**How we implemented:**
```kotlin
@Service
class VideoEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    fun sendNewVideoEvent(videoId: String, category: String) {
        val message = "$category:$videoId"
        kafkaTemplate.send("new-video", message)
        logger.info("📤 Sent Kafka event: $message")
    }
}
```

**When producer fires:**
```
YouTubeService saves new video
→ Calls videoEventProducer.sendNewVideoEvent()
→ Message "F1:abc123" sent to "new-video" topic
→ Producer doesn't wait for consumer ✅
→ Returns immediately ✅
```

**Why async producer:**
```
✅ YouTube fetch not blocked by notification
✅ Fast response to user
✅ Producer doesn't care if consumer is slow
```

---

### 5.4 Consumer

**What:**
```
Component that reads messages from Kafka topic
and processes them
```

**How we implemented:**
```kotlin
@Service
class VideoEventConsumer {
    @KafkaListener(topics = ["new-video"], 
                   groupId = "controlled-feed-group")
    fun consumeNewVideoEvent(message: String) {
        val parts = message.split(":")
        val category = parts[0]
        val videoId = parts[1]
        logger.info("📥 New $category video: $videoId")
    }
}
```

**How consumer works:**
```
New message in "new-video" topic
→ Consumer wakes up automatically
→ Processes message
→ Logs notification
→ Ready for next message
```

**Consumer Group:**
```
groupId = "controlled-feed-group"

Multiple consumers in same group
→ Each message processed by ONE consumer
→ Load balanced automatically ✅

Multiple consumers in different groups
→ Each message processed by ALL groups
→ Different services get same event ✅
```

**Why Consumer Groups:**
```
✅ Scale consumers horizontally
✅ Different services can react to same event
✅ Load balancing built in
✅ Fault tolerant
```

---

### 5.5 Topics

**What:**
```
Category or folder for messages
Like a WhatsApp group for specific topic
```

**Topic we created:**
```kotlin
@Bean
fun newVideoTopic(): NewTopic {
    return NewTopic("new-video", 1, 1)
    //              name    partitions  replicas
}
```

**Parameters:**
```
"new-video" → topic name
1           → number of partitions
1           → replication factor
```

**Why Topics:**
```
✅ Organize messages by type
✅ Different consumers for different topics
✅ Easy to add new event types
```

**How to improve:**
```
→ Add more topics:
   "new-article"     → RSS feed events
   "user-registered" → new user events
   "cache-evict"     → cache invalidation events
→ Increase partitions for more parallelism
→ Increase replicas for fault tolerance
```

---

### 5.6 Message Format

**What we used:**
```
Simple string format:
"F1:abc123"
→ category:videoId

Split on consumer:
parts[0] = "F1"
parts[1] = "abc123"
```

**How to improve:**
```
→ Use JSON format for richer messages:
{
    "videoId": "abc123",
    "category": "F1",
    "title": "Race Highlights",
    "timestamp": "2025-03-01T10:00:00"
}
→ Use Avro schema for type safety
→ Add message version for backward compatibility
```

---

### 5.7 Async Processing

**What:**
```
Producer sends message and continues
Consumer processes in background
Both work independently
```

**In our project:**
```
User requests /api/youtube/f1
→ YouTubeService fetches videos (sync)
→ Saves to DB (sync)
→ Sends Kafka event (async) → returns immediately
→ Consumer processes event in background
→ User gets response fast ✅
```

**Why Async:**
```
✅ User doesn't wait for notification
✅ Better response times
✅ Consumer can be slow → doesn't affect user
✅ Decoupled processing
```

---

## Summary Table

| Concept | Problem Solved | Implementation |
|---------|---------------|----------------|
| Event Driven | Tight coupling | Kafka events |
| Producer | Send events | KafkaTemplate |
| Consumer | Process events | @KafkaListener |
| Topics | Organize messages | NewTopic bean |
| Async Processing | Slow operations | Background consumer |
| Consumer Groups | Load balancing | groupId config |

---

## How Kafka Improved Our Project

```
Before Kafka:
→ YouTubeService directly calls everything
→ Tight coupling ❌
→ One failure cascades ❌
→ Hard to add new features ❌

After Kafka:
→ YouTubeService just fires event ✅
→ Any service can listen ✅
→ Easy to add new consumers ✅
→ Failures isolated ✅
→ Async → faster responses ✅
```

---

## Real World Kafka Usage

```
Netflix  → track viewing history → recommendations
Uber     → driver location updates every second
LinkedIn → activity feed updates
Twitter  → tweet notifications
Our App  → new video notifications ✅
```

---
