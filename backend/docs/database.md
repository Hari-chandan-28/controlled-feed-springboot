## What to Include in DB Documentation

Even though it's not DB heavy, document these:

---
## Structure of [db.md]

```
1. Overview
2. Database Details
3. Tables
4. Relationships
5. Indexes
6. Design Decisions
```

---

## 1. Overview
```
Database: MySQL 8.x
Name: controlled_feed_db
ORM: Hibernate (JPA)
Tables: 4
```

---

## 2. Tables We Have

| Table | Description |
|-------|-------------|
| `users` | Stores user account details |
| `profiles` | Stores user profile and preferences |
| `videos` | Stores YouTube videos |
| `articles` | Stores RSS news articles |

---

## 3. Relationships

```
users ──── profiles   (one to one)
users ──── videos     (no direct relation)
profiles ── videos    (profile genres match video category)
```

---

## 4. Indexes We Added

| Table | Index | Column | Type |
|-------|-------|--------|------|
| users | idx_user_email | email | Unique |
| profiles | idx_profile_user_id | user_id | Unique |
| videos | idx_video_id | video_id | Unique |
| videos | idx_video_category | category | Normal |
| videos | idx_video_published_at | published_at | Normal |

---

## 5. Why Indexes

```
idx_user_email       → fast login lookup
idx_profile_user_id  → fast profile fetch
idx_video_id         → fast duplicate check
idx_video_category   → fast feed query by genre
idx_video_published_at → fast sorting by date
```

---

## 6. Design Decisions

```
* No direct FK between users and videos
   → videos are shared across all users
   → filtering done via profile genres

* VideoCategory as ENUM
   → only F1 and CRICKET allowed
   → prevents invalid data

* Serializable on all entities
   → required for Redis caching

* auto-ddl = update
   → Hibernate auto creates/updates tables
   → no manual SQL scripts needed
```

---
## Table Structure for `db.md`

For each table include:

---

## Template for Each Table

```
Table Name
Short description
Columns table
Sample data (optional)
```

---

## Column Table Format

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User email |

---

## Our 4 Tables

---

### 1. `users`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(255) | NOT NULL | User full name |
| email | VARCHAR(255) | NOT NULL, UNIQUE | User email address |
| password | VARCHAR(255) | NOT NULL | Bcrypt hashed password |
| role | VARCHAR(50) | NOT NULL | User role (USER/ADMIN) |

---

### 2. `profiles`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| user_id | BIGINT | NOT NULL, UNIQUE, FK → users.id | Reference to user |
| bio | TEXT | NULLABLE | User bio |
| genres | VARCHAR(255) | NOT NULL | Comma separated genres (F1, CRICKET) |
| profile_picture | VARCHAR(255) | NULLABLE | Path to profile picture |

---

### 3. `videos`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| video_id | VARCHAR(255) | NOT NULL, UNIQUE | YouTube video ID |
| title | VARCHAR(255) | NOT NULL | Video title |
| description | TEXT | NULLABLE | Video description |
| thumbnail_url | VARCHAR(500) | NULLABLE | Thumbnail image URL |
| published_at | VARCHAR(255) | NULLABLE | Published date from YouTube |
| channel_title | VARCHAR(255) | NULLABLE | YouTube channel name |
| category | ENUM | NOT NULL | F1 or CRICKET |

---

### 4. `articles`

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| guid | VARCHAR(500) | NOT NULL, UNIQUE | Unique article identifier from RSS |
| title | VARCHAR(255) | NOT NULL | Article title |
| description | TEXT | NULLABLE | Article summary |
| link | VARCHAR(500) | NULLABLE | Original article URL |
| image_url | VARCHAR(500) | NULLABLE | Article image URL |
| published_at | VARCHAR(255) | NULLABLE | Published date from RSS |
| source | VARCHAR(255) | NULLABLE | Source name (Autosport, BBC etc.) |
| category | ENUM | NOT NULL | F1 or CRICKET |


## Indexes Summary

| Table | Index Name | Column | Type | Purpose |
|-------|-----------|--------|------|---------|
| users | idx_user_email | email | UNIQUE | Fast login lookup |
| profiles | idx_profile_user_id | user_id | UNIQUE | Fast profile fetch |
| videos | idx_video_id | video_id | UNIQUE | Duplicate prevention |
| videos | idx_video_category | category | NORMAL | Fast feed query |
| videos | idx_video_published_at | published_at | NORMAL | Sort by latest |

---
