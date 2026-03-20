# PROJECT NAME
FAN FEED

# ABOUT THIS PROJECT
* This is my first backend project, built to understand core concepts like API development, system design and 
real-world data handling and to solve my problem .
# PROBLEM STATEMENT
* Existing platforms like social media apps provide sports updates, but they also contain a large  amount  of  distracting 
and irrelevant content. This reduces focus and productivity, as users often get diverted away from their original intent.
There is a need for a dedicated solution that delivers only relevant sports information (such as F1 and Cricket updates)
without unnecessary distractions.

# WHY I BUILT THIS
* I built this project to avoid distractions caused by social media while following sports updates. 
* Platforms like Instagram mix useful content with a lot of irrelevant information, which affects focus and productivity. So, I need to fix my problem.
* To solve this, I created a dedicated solution that provides only F1 and Cricket updates.
* This also helped me learn and apply new technologies,system design concepts by working with external APIs and building a personalized feed.
* I want to work with LLMs , so I have added the Gemini API and try to use them and connected to the backend .

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
    YouTube Data API v3             → F1 + Cricket videos
    Ergast/Jolpi API                → F1 season data
    OpenF1 API                      → F1 live race data
    CricAPI                         → Cricket live scores (late responses and limited calls)
    Rome Library                    → RSS feed parsing (8 sources)
    Gemini (2.5 flash model) API    → F1 and Cricket related AI chatbot 
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
   ## ChatBot
    Only answer the questions which are related to the f1 and cricket
    Deny answering other stuff

   ## Production Grade
    Redis caching with TTL
    Rate limiting per IP
    Retry mechanism
    Circuit Breaker
    Health check endpoint
    Kafka event streaming
    Swagger API docs
    Database indexing

# How to Run
    This project requires MySQL, Redis and Kafka
    running before starting the application.
### Prerequisites Check
    - Java 21 installed
    - MySQL running on port 3306
    - Redis running on port 6379
    - Kafka running on port 9092
### Steps
    Step 1 — Clone
    Step 2 — Setup Database
    Step 3 — Start Redis
    Step 4 — Start Kafka
    Step 5 — Configure properties
    Step 6 — Run app

##  Content Documentation 
    I have used the Claude and ChatGPT for making the api,architecture, database,progress,setup documentation which are present in the docs package .
    I have also too help of GPTs for making more clear systemDesign Documentation for each system design concept , these are present in the systemDesignTopicsDocs package, In that i made some dedicated files for better understanding of the features and their benifits.
    I have used AI because i need to save time and need quality output.

## Time Taken
    I took 32 Days to complete this project and with some leftoff features mentioned clearly in the progress.md file which is present in the docs package.