
### Base URL
    http://localhost:8080
### Authentication
    All APIs except signup, login, health need:
    Header: Authorization: Bearer <token>
    Standard Error Format
    **  json{
        "status": 400,
        "error": "Bad Request",
        "message": "error description"
        }

## Complete API Table

| # | Method | Endpoint | Auth | Description | Rate Limit | Cache |
|---|--------|----------|------|-------------|------------|-------|
| 1 | POST | `/api/auth/signup` | ❌ | Register new user | 5/min | ❌ |
| 2 | POST | `/api/auth/login` | ❌ | Login and get token | 10/min | ❌ |
| 3 | POST | `/api/profile/create` | ✅ | Create user profile | 30/min | ❌ |
| 4 | GET | `/api/profile/me` | ✅ | Get current user profile | 30/min | ❌ |
| 5 | POST | `/api/profile/upload-picture` | ✅ | Upload profile picture | 30/min | ❌ |
| 6 | GET | `/api/youtube/f1` | ✅ | Fetch and store F1 videos | 10/min | ❌ |
| 7 | GET | `/api/youtube/icc` | ✅ | Fetch and store Cricket videos | 10/min | ❌ |
| 8 | GET | `/api/feed` | ✅ | Get personalized feed | 60/min | 5 mins |
| 9 | GET | `/api/rss/f1` | ✅ | Fetch F1 news articles | 30/min | ❌ |
| 10 | GET | `/api/rss/cricket` | ✅ | Fetch Cricket news articles | 30/min | ❌ |
| 11 | GET | `/api/f1/standings` | ✅ | Driver championship standings | 30/min | 1 hour |
| 12 | GET | `/api/f1/constructors` | ✅ | Constructor standings | 30/min | 1 hour |
| 13 | GET | `/api/f1/results` | ✅ | Latest race results | 30/min | 1 hour |
| 14 | GET | `/api/f1/schedule` | ✅ | Full season schedule | 30/min | 1 hour |
| 15 | GET | `/api/f1/live/positions` | ✅ | Live driver positions on circuit | 30/min | 3 secs |
| 16 | GET | `/api/f1/live/timing` | ✅ | Live lap and sector timing | 30/min | 3 secs |
| 17 | GET | `/api/f1/live/intervals` | ✅ | Live driver gaps and intervals | 30/min | 3 secs |
| 18 | GET | `/api/cricket/live` | ✅ | Live cricket matches | 30/min | 30 secs |
| 19 | GET | `/api/cricket/upcoming` | ✅ | Upcoming cricket matches | 30/min | 1 hour |
| 20 | GET | `/api/cricket/scorecard/{matchId}` | ✅ | Full match scorecard | 30/min | 30 secs |
| 21 | GET | `/actuator/health` | ❌ | App health status | 30/min | ❌ |
## 🔐 Auth APIs

### 1. Signup
```
POST /api/auth/signup
Auth Required: No
Request:
json{
"name": "Hari Chandan",
"email": "hari@gmail.com",
"password": "123456"
}
Response 200:
json{
"token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
Errors:
```
400 → name/email/password empty
400 → invalid email format
400 → password less than 6 characters
409 → email already exists
429 → rate limit exceeded (5 requests/min)
```

---

### 2. Login
```
POST /api/auth/login
Auth Required: No
Request:
json{
"email": "hari@gmail.com",
"password": "123456"
}
Response 200:
json{
"token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
Errors:
```
400 → email/password empty
401 → invalid credentials
429 → rate limit exceeded (10 requests/min)
```

---

## 👤 Profile APIs

### 3. Create Profile
```
POST /api/profile/create
Auth Required: Yes
Request:
json{
"bio": "I love F1 and Cricket!",
"genres": ["F1", "CRICKET"]
}
Response 200:
json{
"id": 1,
"bio": "I love F1 and Cricket!",
"genres": ["F1", "CRICKET"],
"profilePicture": ""
}
```
Errors:
```
401 → missing or invalid token
409 → profile already exists
429 → rate limit exceeded
```

---

### 4. Get Profile
```
GET /api/profile/me
Auth Required: Yes
Response 200:
json{
"id": 1,
"bio": "I love F1 and Cricket!",
"genres": ["F1", "CRICKET"],
"profilePicture": "uploads/profile-pictures/abc.jpg"
}
```
Errors:
```
401 → missing or invalid token
404 → profile not found
```

---

### 5. Upload Profile Picture
```
POST /api/profile/upload-picture
Auth Required: Yes
Content-Type: multipart/form-data
```
Request:
```
KEY    TYPE    VALUE
file   File    → select image
Response 200:
json{
"message": "Profile picture uploaded successfully",
"path": "uploads/profile-pictures/abc.jpg"
}
```
Errors:
```
400 → file too large (max 5MB)
400 → invalid file type
401 → missing or invalid token
```

---

## 📺 YouTube APIs

### 6. Fetch F1 Videos
```
GET /api/youtube/f1
Auth Required: Yes
Response 200:
json[
{
"id": 1,
"videoId": "abc123",
"title": "F1 Race Highlights",
"description": "...",
"thumbnailUrl": "https://...",
"publishedAt": "2025-03-01",
"channelTitle": "Formula 1",
"category": "F1"
}
]
```
Errors:
```
401 → missing or invalid token
429 → rate limit exceeded (10 requests/min)
500 → YouTube API error
```

---

### 7. Fetch ICC Videos
```
GET /api/youtube/icc
Auth Required: Yes
Response 200:
json[
{
"id": 1,
"videoId": "xyz789",
"title": "Cricket Match Highlights",
"description": "...",
"thumbnailUrl": "https://...",
"publishedAt": "2025-03-01",
"channelTitle": "ICC",
"category": "CRICKET"
}
]
```
Errors:
```
401 → missing or invalid token
429 → rate limit exceeded (10 requests/min)
500 → YouTube API error
```

---

## 🎯 Feed API

### 8. Get Personalized Feed
```
GET /api/feed?page=0&size=10
Auth Required: Yes
```
Query Params:
```
page → page number (default 0)
size → items per page (default 10)
Response 200:
json[
{
"id": 1,
"videoId": "abc123",
"title": "F1 Race Highlights",
"description": "...",
"thumbnailUrl": "https://...",
"publishedAt": "2025-03-01",
"channelTitle": "Formula 1",
"category": "F1"
}
]
```
Errors:
```
401 → missing or invalid token
404 → profile not found
429 → rate limit exceeded (60 requests/min)
```

---

## 📰 RSS Feed APIs

### 9. Fetch F1 Articles
```
GET /api/rss/f1
Auth Required: Yes
Response 200:
json[
{
"id": 1,
"guid": "https://autosport.com/...",
"title": "Verstappen wins Japanese GP",
"description": "...",
"link": "https://autosport.com/...",
"imageUrl": "https://...",
"publishedAt": "Sun Mar 01 2025",
"source": "Autosport",
"category": "F1"
}
]
```
Errors:
```
401 → missing or invalid token
500 → RSS feed unavailable
```

---

### 10. Fetch Cricket Articles
```
GET /api/rss/cricket
Auth Required: Yes
Response 200:
json[
{
"id": 1,
"guid": "https://espncricinfo.com/...",
"title": "India wins test series",
"description": "...",
"link": "https://espncricinfo.com/...",
"imageUrl": "https://...",
"publishedAt": "Sun Mar 01 2025",
"source": "ESPNCricinfo",
"category": "CRICKET"
}
]
```
Errors:
```
401 → missing or invalid token
500 → RSS feed unavailable
```

---

## 🏎️ F1 Dashboard APIs

### 11. Driver Standings
```
GET /api/f1/standings
Auth Required: Yes
Cache: 1 hour
Response 200:
json[
{
"position": "1",
"driverName": "Max Verstappen",
"team": "Red Bull",
"points": "77",
"wins": "3",
"nationality": "Dutch",
"podiums": 5
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open (Ergast API down)
```

---

### 12. Constructor Standings
```
GET /api/f1/constructors
Auth Required: Yes
Cache: 1 hour
Response 200:
json[
{
"position": "1",
"teamName": "Red Bull",
"nationality": "Austrian",
"points": "137",
"wins": "3",
"podiums": 7
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 13. Latest Race Results
```
GET /api/f1/results
Auth Required: Yes
Cache: 1 hour
Response 200:
json[
{
"position": "1",
"driverName": "Max Verstappen",
"team": "Red Bull",
"time": "1:30:55.026",
"fastestLap": "1:17.775",
"points": "25"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 14. Race Schedule
```
GET /api/f1/schedule
Auth Required: Yes
Cache: 1 hour
Response 200:
json[
{
"raceName": "Bahrain Grand Prix",
"circuit": "Bahrain International Circuit",
"country": "Bahrain",
"date": "2025-03-02",
"time": "15:00:00Z"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 15. Live Driver Positions
```
GET /api/f1/live/positions
Auth Required: Yes
Cache: 3 seconds
Note: Returns data only during race weekend
Response 200:
json[
{
"driverNumber": 1,
"driverName": "Max Verstappen",
"teamName": "Red Bull Racing",
"position": 1,
"x": 4523.0,
"y": -1234.0,
"z": 0.0,
"date": "2025-03-02T15:23:11"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 16. Live Timing
```
GET /api/f1/live/timing
Auth Required: Yes
Cache: 3 seconds
Note: Returns data only during race weekend
Response 200:
json[
{
"driverNumber": 1,
"lapNumber": 23,
"lapDuration": 87.456,
"sector1": 28.123,
"sector2": 31.456,
"sector3": 27.877,
"isPitOutLap": false,
"date": "2025-03-02T15:23:11"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 17. Live Intervals
```
GET /api/f1/live/intervals
Auth Required: Yes
Cache: 3 seconds
Note: Returns data only during race weekend
Response 200:
json[
{
"driverNumber": 1,
"gapToLeader": "0.000",
"interval": "0.000",
"date": "2025-03-02T15:23:11"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

## 🏏 Cricket APIs

### 18. Live Matches
```
GET /api/cricket/live
Auth Required: Yes
Cache: 30 seconds
Response 200:
json[
{
"id": "abc123",
"name": "India vs Australia",
"matchType": "T20",
"status": "India won by 6 wickets",
"venue": "Wankhede Stadium",
"teams": ["India", "Australia"],
"scores": [
{
"inning": "India Inning 1",
"runs": 186,
"wickets": 4,
"overs": 18.2
}
],
"dateTime": "2025-03-01T14:00:00"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open (CricAPI down)
```

---

### 19. Upcoming Matches
```
GET /api/cricket/upcoming
Auth Required: Yes
Cache: 1 hour
Response 200:
json[
{
"id": "xyz789",
"name": "IPL 2025 - MI vs CSK",
"matchType": "T20",
"status": "Match not started",
"venue": "Wankhede Stadium",
"teams": ["Mumbai Indians", "Chennai Super Kings"],
"scores": [],
"dateTime": "2025-04-01T19:30:00"
}
]
```
Errors:
```
401 → missing or invalid token
503 → Circuit Breaker open
```

---

### 20. Match Scorecard
```
GET /api/cricket/scorecard/{matchId}
Auth Required: Yes
Cache: 30 seconds
```
Path Param:
```
matchId → match ID from live or upcoming response
Response 200:
json{
"matchId": "abc123",
"name": "India vs Australia",
"status": "India won by 6 wickets",
"venue": "Wankhede Stadium",
"batting": [
{
"player": "Virat Kohli",
"runs": 82,
"balls": 54,
"fours": 8,
"sixes": 2,
"strikeRate": 151.85,
"dismissal": "c Smith b Hazlewood"
}
],
"bowling": [
{
"player": "Josh Hazlewood",
"overs": 4.0,
"maidens": 0,
"runs": 28,
"wickets": 2,
"economy": 7.0
}
]
}
```
Errors:
```
401 → missing or invalid token
404 → match not found
503 → Circuit Breaker open
```

---

## ❤️ Health Check

### 21. Health Check
```
GET /actuator/health
Auth Required: No
Response 200:
json{
"status": "UP",
"components": {
"db": {
"status": "UP"
},
"redis": {
"status": "UP"
},
"diskSpace": {
"status": "UP"
}
}
}
```
Errors:
```
503 → one or more components DOWN