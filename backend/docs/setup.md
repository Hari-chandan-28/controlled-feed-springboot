# PREREQUISITES
## Required Software
    Java 21          → runtime
    MySQL 8.x        → database
    Redis            → caching
    Apache Kafka     → message queue
## Required Accounts / API Keys
    Google Cloud Console  → YouTube Data API v3 key
    CricAPI               → Cricket live scores key (free)
## Required Tools
    IntelliJ IDEA    → IDE (recommended)
    Postman          → API testing
    Git              → version control
    Maven            → build tool (bundled with IntelliJ)
## Versions Used
    Java        → 21
    Spring Boot → 4.x
    Kotlin      → 2.x
    MySQL       → 8.x
    Redis       → 5.x
    Kafka       → 3.7.x

# How to Run This Project
## Step 1 — Clone the Repository
    bashgit clone https://github.com/Hari-chandan-28/controlled-feed-springboot.git
    cd controlled-feed-springboot
## Step 2 — Setup MySQL
    sql CREATE DATABASE controlled_feed_db;
## Step 3 — Setup Redis
    bash# Windows
    redis-server
    * Or download from 
[https://github.com/tporadowski/redis/releases] 
## Step 4 — Setup Kafka
    * bash# Terminal 1 - Start Zookeeper
    cd C:\kafka
    .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
    * Terminal 2 - Start Kafka
    cd C:\kafka
    .\bin\windows\kafka-server-start.bat .\config\server.properties
## Step 5 — Configure application.properties
    properties
### Database
    spring.datasource.url=jdbc:mysql://localhost:3306/controlled_feed_db
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password

### YouTube API
    youtube.api.key=your_youtube_api_key

### Cricket API
    cricket.api.key=your_cricket_api_key

### JWT
    jwt.secret=your_jwt_secret_key
## Step 6 — Run the App
    bash# Using Maven
    mvn spring-boot:run

### Or in IntelliJ
    Run → BackendApplication.kt
### Access
    Swagger UI  → http://localhost:8080/swagger-ui
    Health Check → http://localhost:8080/actuator/health
# ENVIRONMENT VARIABLES
### App
spring.application.name=controlled-feed-springboot

### Database
    spring.datasource.url=jdbc:mysql://localhost:3306/controlled_feed_db
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password

### JPA / Hibernate
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.show-sql=true
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

### Security
    spring.security.user.name=admin
    spring.security.user.password=admin

### JWT
    jwt.secret=your_secret_key_minimum_32_characters_long
    jwt.expiration=86400000

### File Upload
    spring.servlet.multipart.enabled=true
    spring.servlet.multipart.max-file-size=5MB
    spring.servlet.multipart.max-request-size=5MB

###  Upload directory
    file.upload-dir=uploads/profile-pictures

### YouTube
    youtube.api.key=your_youtube_api_key
    youtube.f1.channel.id=UCB_qr75-ydFVKSF9Dmo6izg
    youtube.icc.channel.id=UCiWrjBhlICf_L_RK5y6Xo_g
### Error Handling
    spring.web.error.include-message=always
    spring.web.error.include-binding-errors=always
### Swagger
    springdoc.api-docs.path=/api-docs
    springdoc.swagger-ui.path=/swagger-ui
    springdoc.swagger-ui.enabled=true
### Redis
    spring.data.redis.host=localhost
    spring.data.redis.port=6379
    spring.cache.type=redis
    spring.cache.redis.time-to-live=300000
### Circuit Breaker Config
    resilience4j.circuitbreaker.instances.youtubeService.sliding-window-size=5
    resilience4j.circuitbreaker.instances.youtubeService.failure-rate-threshold=50
    resilience4j.circuitbreaker.instances.youtubeService.wait-duration-in-open-state=30s
    resilience4j.circuitbreaker.instances.youtubeService.permitted-number-of-calls-in-half-open-state=2
    resilience4j.circuitbreaker.instances.youtubeService.automatic-transition-from-open-to-half-open-enabled=true
### Health Check Config
    management.endpoints.web.exposure.include=health,info
    management.endpoint.health.show-details=always
    management.health.redis.enabled=true
    management.health.db.enabled=true
### Kafka Config
    spring.kafka.bootstrap-servers=localhost:9092
    spring.kafka.consumer.group-id=controlled-feed-group
    spring.kafka.consumer.auto-offset-reset=earliest
    spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
    spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
    spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
    spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
### F1 Circuit Breaker
    resilience4j.circuitbreaker.instances.f1Service.sliding-window-size=10
    resilience4j.circuitbreaker.instances.f1Service.failure-rate-threshold=80
    resilience4j.circuitbreaker.instances.f1Service.wait-duration-in-open-state=10s
    resilience4j.circuitbreaker.instances.f1Service.permitted-number-of-calls-in-half-open-state=5
    resilience4j.circuitbreaker.instances.f1Service.automatic-transition-from-open-to-half-open-enabled=true

### Temporary API Only for learning can be removed in the future

### CricAPI
    cricket.api.key=your_cricapi_key
    cricket.api.base.url=https://api.cricapi.com/v1
### Cricket Circuit Breaker
    resilience4j.circuitbreaker.instances.cricketService.sliding-window-size=5
    resilience4j.circuitbreaker.instances.cricketService.failure-rate-threshold=50
    resilience4j.circuitbreaker.instances.cricketService.wait-duration-in-open-state=30s
    resilience4j.circuitbreaker.instances.cricketService.permitted-number-of-calls-in-half-open-state=2
    resilience4j.circuitbreaker.instances.cricketService.automatic-transition-from-open-to-half-open-enabled=true