# PROJECT NAME
FAN FEED

# ABOUT THIS PROJECT
* This is my first backend project, built to understand core concepts like API development, system design basics, and 
real-world data handling and to solve my problem .
# PROBLEM STATEMENT
* Existing platforms like social media apps provide sports updates, but they also contain a large  amount  of  distracting 
and irrelevant content. This reduces focus and productivity, as users often get diverted away from their original intent.
There is a need for a dedicated solution that delivers only relevant sports information (such as F1 and Cricket updates)
without unnecessary distractions.

# WHY I BUILT THIS
* I built this project to avoid distractions caused by social media while following sports updates. 
* Platforms like Instagram mix useful content with a lot of irrelevant information, which affects focus and productivity.
* To solve this, I created a dedicated solution that provides only F1 and Cricket updates.
* This also helped me learn and apply new technologies by working with external APIs and building a personalized feed.

# TECH STACK 
   ## Core
    Spring Boot 4.x    → backend framework
    Kotlin             → programming language
    MySQL              → primary database
    JWT                → authentication
   ## Caching & Performance
    Redis              → caching layer
    Bucket4j           → rate limiting
   ## Resilience
    Spring Retry       → retry with exponential backoff
    Resilience4j       → circuit breaker
    Spring Actuator    → health check
   ## Messaging
    Apache Kafka       → event streaming / message queue
   ## External APIs
    YouTube Data API v3    → F1 + Cricket videos
    Ergast/Jolpi API       → F1 season data
    OpenF1 API             → F1 live race data
    CricAPI                → Cricket live scores (late responses and limited calls)
    Rome Library           → RSS feed parsing (8 sources)
   ## Documentation & Testing
    Springdoc OpenAPI  → Swagger UI
    Postman            → API testing
   ## Build & Tools
    Maven              → build tool
    IntelliJ IDEA      → IDE
    Git + GitHub       → version control
    WebFlux WebClient  → HTTP client for external APIs

# Features of Our Project
   ## Authentication
    User signup and login
    JWT token based security
    Password encryption
   ## Profile
    Create user profile
    Set genre preferences (F1 / Cricket)
    Upload profile picture
   ## YouTube Integration
    Auto fetch F1 and Cricket videos every 10 mins
    Duplicate prevention
    Retry with exponential backoff
    Circuit Breaker protection
   ## RSS News Feed
    F1 news from 4 sources
    Cricket news from 4 sources
    Auto fetched every 10 mins
   ## F1 Dashboard
    Driver championship standings + podiums
    Constructor standings + podiums
    Latest race results
    Full season schedule
    Live driver positions on circuit
    Live lap timing and sector times
    Live driver intervals and gaps
   ## Cricket
    Live match scores(late responses and limited calls)
    Full batting scorecard 
    Full bowling scorecard 
    Upcoming matches (not team specific , some random matches info)
   ## Personalized Feed
    Feed based on user genre preferences
    Paginated responses
    Redis cached
   ## Production Grade
    Redis caching with TTL
    Rate limiting per IP
    Retry mechanism
    Circuit Breaker
    Health check endpoint
    Kafka event streaming
    Swagger API docs
    Database indexing

# How to Run This Project
  ### Step 1 — Clone the Repository
    bashgit clone https://github.com/Hari-chandan-28/controlled-feed-springboot.git
    cd controlled-feed-springboot
  ### Step 2 — Setup MySQL
    sql CREATE DATABASE controlled_feed_db;
  ### Step 3 — Setup Redis
    bash# Windows
    redis-server
    * Or download from 
[https://github.com/tporadowski/redis/releases]  
  ### Step 4 — Setup Kafka
    * bash# Terminal 1 - Start Zookeeper
    cd C:\kafka
    .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
    * Terminal 2 - Start Kafka
    cd C:\kafka
    .\bin\windows\kafka-server-start.bat .\config\server.properties
  ### Step 5 — Configure application.properties
    properties
## Database
    spring.datasource.url=jdbc:mysql://localhost:3306/controlled_feed_db
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password

## YouTube API
    youtube.api.key=your_youtube_api_key

## Cricket API
    cricket.api.key=your_cricket_api_key

## JWT
    jwt.secret=your_jwt_secret_key
  ### Step 6 — Run the App
    bash# Using Maven
    mvn spring-boot:run

## Or in IntelliJ
Run → BackendApplication.kt
## Access
    Swagger UI  → http://localhost:8080/swagger-ui
    Health Check → http://localhost:8080/actuator/health
