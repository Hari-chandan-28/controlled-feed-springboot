## 6. External API Integration

---

## What is External API Integration?

External API Integration is the process of **connecting your application to third party services** to fetch or send data.

```
Without External APIs:
→ Build everything yourself
→ Collect your own F1 data ❌
→ Record your own cricket scores ❌
→ Impossible for one developer!

With External APIs:
→ YouTube handles video storage ✅
→ Ergast handles F1 data ✅
→ CricAPI handles cricket scores ✅
→ Focus on your own logic ✅
```

---

## Simple Analogy

```
Building a house:
→ You don't make your own bricks
→ You don't manufacture your own windows
→ You buy from suppliers and assemble

Building software:
→ You don't collect your own F1 data
→ You don't record your own cricket scores
→ You use APIs and build on top ✅
```

---

## All External APIs We Used

```
1. YouTube Data API v3    → F1 + Cricket videos
2. Ergast/Jolpi API       → F1 season data
3. OpenF1 API             → F1 live race data
4. CricAPI                → Cricket live scores
5. RSS Feeds (8 sources)  → F1 + Cricket news
6. Gemini API             → AI chatbot
```

---

## Subtopics We Used

---

### 6.1 WebClient (Reactive HTTP Client)

**What:**
```
Spring WebFlux WebClient
Modern async HTTP client
Replaced old RestTemplate
```

**How we used it:**
```kotlin
private val webClient = WebClient.builder()
    .baseUrl("https://api.jolpi.ca/ergast/f1")
    .build()

val response = webClient.get()
    .uri("/current/driverStandings.json")
    .retrieve()
    .bodyToMono(Map::class.java)
    .block()
```

**Why WebClient over RestTemplate:**
```
✅ Non-blocking async calls
✅ Better error handling
✅ Fluent API → readable code
✅ Supports reactive streams
✅ Spring recommended approach
```

**Problems with old RestTemplate:**
```
❌ Blocking → thread waits for response
❌ Deprecated in newer Spring versions
❌ Less flexible error handling
```

---

### 6.2 YouTube Data API v3

**What it provides:**
```
Search videos by channel
Video metadata (title, description, thumbnail)
Channel information
Video statistics
```

**How we used it:**
```kotlin
webClient.get()
    .uri { uriBuilder ->
        uriBuilder
            .path("/search")
            .queryParam("key", apiKey)
            .queryParam("channelId", channelId)
            .queryParam("part", "snippet")
            .queryParam("order", "date")
            .queryParam("maxResults", 10)
            .queryParam("type", "video")
            .build()
    }
```

**Key parameters:**
```
key        → API key for authentication
channelId  → F1 or ICC channel ID
part       → what data to return (snippet)
order      → date = latest videos first
maxResults → how many videos to fetch
type       → video only (not playlists)
```

**Channel IDs we use:**
```
F1  → UCB_qr75-ydFVKSF9Dmo6izg
ICC → UCiWrjBhlICf_L_RK5y6Xo_g
```

**Resilience added:**
```
✅ Retry with exponential backoff
✅ Circuit Breaker with fallback
✅ Duplicate prevention via videoId check
```

**Limitations:**
```
❌ 10,000 units/day free quota
❌ Search costs 100 units per call
❌ = 100 free searches per day
→ Solved by scheduler every 10 minutes ✅
```

---

### 6.3 Ergast/Jolpi F1 API

**What it provides:**
```
F1 season data since 1950
Driver standings
Constructor standings
Race results
Race schedule
Lap times
Pit stops
```

**Why Jolpi mirror:**
```
Original Ergast API → being deprecated
Jolpi → community maintained mirror
→ Same endpoints
→ More reliable going forward ✅
```

**Endpoints we used:**
```
/current/driverStandings.json     → driver standings
/current/constructorStandings.json→ constructor standings
/current/last/results.json        → latest race results
/current.json                     → full season schedule
/current/results.json?limit=1000  → all results for podiums
```

**Why no API key needed:**
```
✅ Completely free and open
✅ No rate limits for reasonable usage
✅ No signup required
✅ Perfect for learning projects
```

**Resilience added:**
```
✅ Circuit Breaker with fallback
✅ Redis cache 1 hour TTL
→ Only 24 API calls per day maximum ✅
```

---

### 6.4 OpenF1 API

**What it provides:**
```
Real time F1 data during race weekend
Driver positions (X, Y coordinates)
Lap times and sector times
Driver intervals and gaps
Car telemetry (speed, throttle, brake)
Weather data
```

**Endpoints we used:**
```
/position?session_key=latest   → live positions
/laps?session_key=latest       → live lap times
/intervals?session_key=latest  → live gaps
/drivers?session_key=latest    → driver info
```

**Session key:**
```
session_key=latest → current/most recent session
session_key=9158   → specific session by ID
```

**Why OpenF1:**
```
✅ Completely free
✅ No API key needed
✅ Real time data during races
✅ X,Y coordinates for circuit visualization
```

**Limitations:**
```
❌ Only available during race weekends
❌ Returns empty outside race sessions
→ Handled by returning empty list gracefully ✅
```

**Resilience added:**
```
✅ Circuit Breaker with fallback
✅ Redis cache 3 seconds TTL
→ Frontend polls every 3 secs → only 1 API call per 3 secs ✅
```

---

### 6.5 CricAPI

**What it provides:**
```
Live cricket match scores
Full batting and bowling scorecard
Upcoming matches
Match details
Player statistics
```

**Endpoints we used:**
```
/currentMatches → live matches
/matches        → upcoming matches
/match_scorecard → full scorecard by match ID
```

**Free tier limits:**
```
100 calls/day free
→ Enough for:
   → Live polling every 30 seconds = 2880 calls/day ❌ too many
   → We cache 30 seconds → reduces to ~2880/30 = 96 calls/day ✅
```

**Resilience added:**
```
✅ Circuit Breaker with fallback
✅ Redis cache 30 seconds TTL
✅ Stays within free tier limits ✅
```

---

### 6.6 RSS Feed Parsing

**What:**
```
RSS = Really Simple Syndication
Standard format websites use to publish content
Parse XML feed → extract articles
```

**Library used:**
```
Rome library → Java RSS/Atom feed parser
```

**How we used it:**
```kotlin
val feed = SyndFeedInput().build(XmlReader(URL(feedUrl)))
feed.entries.forEach { entry ->
    val title = entry.title
    val description = entry.description?.value
    val link = entry.link
    val publishedDate = entry.publishedDate
}
```

**Sources we use:**

F1 Sources:
```
Autosport       → https://www.autosport.com/rss/f1/news/
Motorsport       → https://www.motorsport.com/rss/f1/news/
BBC Sport F1     → https://www.bbc.co.uk/sport/formula1/rss.xml
Sky Sports F1    → https://feeds.skysports.com/skysports/f1
```

Cricket Sources:
```
ESPNCricinfo     → https://www.espncricinfo.com/rss/content/story/feeds/0.xml
Cricbuzz         → https://www.cricbuzz.com/rss-feeds/cricket-news
BBC Sport Cricket→ https://www.bbc.co.uk/sport/cricket/rss.xml
Sky Sports Cricket→ https://feeds.skysports.com/skysports/cricket
```

**Why multiple sources:**
```
✅ More content for users
✅ If one source fails → others work
✅ Different perspectives on same event
✅ No API key needed for any source
```

**Resilience added:**
```
✅ Each source wrapped in try/catch
✅ One source failing → others continue
✅ Scheduled every 10 minutes
✅ Duplicate prevention via GUID
```

---

### 6.7 Gemini API

**What it provides:**
```
Google's Large Language Model
Generate text responses
Answer questions
Follow instructions
```

**How we used it:**
```kotlin
val requestBody = mapOf(
    "contents" to listOf(
        mapOf(
            "parts" to listOf(
                mapOf("text" to "$systemPrompt\n\nQuestion: $question")
            )
        )
    )
)
```

**Prompt Engineering:**
```
System prompt tells Gemini:
→ Only answer F1 and Cricket questions
→ Refuse other topics politely
→ Keep answers clear and concise
→ Use facts and statistics
```

**Why Prompt Engineering:**
```
✅ No fine-tuning needed
✅ Domain restricted responses
✅ Consistent behavior
✅ Free to implement
```

---

## Summary Table

| API | Data | Auth | Free Limit | Cache TTL |
|-----|------|------|------------|-----------|
| YouTube v3 | F1/Cricket videos | API Key | 100 searches/day | No cache |
| Ergast/Jolpi | F1 season data | None | Unlimited | 1 hour |
| OpenF1 | F1 live data | None | Unlimited | 3 seconds |
| CricAPI | Cricket scores | API Key | 100 calls/day | 30 seconds |
| RSS Feeds | News articles | None | Unlimited | No cache |
| Gemini | AI responses | API Key | Limited free | No cache |

---

## How External APIs Improved Our Project

```
Before External APIs:
→ No F1 data ❌
→ No cricket scores ❌
→ No videos ❌
→ No news articles ❌
→ No AI chatbot ❌

After External APIs:
→ Rich F1 dashboard ✅
→ Live cricket scores ✅
→ Latest videos ✅
→ News from 8 sources ✅
→ AI sports chatbot ✅
→ All without building from scratch ✅
```

---

## Best Practices We Followed

```
✅ Cache responses to reduce API calls
✅ Circuit Breaker to handle API failures
✅ Retry for temporary failures
✅ Duplicate prevention before saving
✅ Scheduled fetching instead of on-demand
✅ Multiple sources for redundancy
✅ Graceful fallback on failure
```

---