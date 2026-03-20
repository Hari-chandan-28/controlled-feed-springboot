## 2. Database Design

---

## What is Database Design?

Database Design is the process of **planning how to store, organize and retrieve data efficiently**.

It answers:
```
What tables do we need?
What columns in each table?
How tables relate to each other?
How to make queries fast?
What constraints to add?
```

---

## Simple Analogy

```
Without Database Design:
→ Like storing everything in one big drawer
→ Finding something takes forever
→ Duplicate data everywhere

With Database Design:
→ Like organized filing cabinets
→ Each drawer has specific purpose
→ Find anything instantly
```

---

## Subtopics We Used

---

### 2.1 Relational Database (MySQL)

**What:**
```
Database that stores data in tables
Tables have rows and columns
Tables can relate to each other
```

**Why MySQL:**
```
✅ Industry standard
✅ ACID compliance → data is always consistent
✅ Strong relationships between data
✅ Mature and well supported
✅ Works perfectly with Spring Boot/JPA
```

**Problems without relational DB:**
```
❌ No data consistency guarantees
❌ Hard to maintain relationships
❌ Duplicate data everywhere
```

**How to improve:**
```
→ Add connection pooling (HikariCP - already included)
→ Add read replicas for heavy read loads
→ Add database backups
```

---

### 2.2 ORM — Hibernate/JPA

**What:**
```
ORM = Object Relational Mapping
Maps Java/Kotlin classes to database tables
No need to write SQL manually
```

**How we used it:**
```
@Entity    → marks class as DB table
@Table     → specifies table name
@Column    → specifies column properties
@Id        → marks primary key
@GeneratedValue → auto increment

Spring Data JPA:
→ findById(), save(), delete() → built in
→ Custom queries → findByCategoryIn()
→ No SQL needed for basic operations
```

**Why ORM:**
```
✅ No manual SQL for basic operations
✅ Database independent code
✅ Auto table creation (ddl-auto=update)
✅ Type safe queries
✅ Reduces boilerplate code
```

**Problems with ORM:**
```
❌ N+1 query problem
❌ Complex queries harder to optimize
❌ Learning curve
❌ Can generate inefficient SQL
```

**How to improve:**
```
→ Use @Query for complex queries
→ Use projections for partial data fetch
→ Enable SQL logging to monitor queries
→ Use fetch = LAZY for relationships
```

---

### 2.3 Database Indexing

**What:**
```
Index = special lookup table that helps
find data faster without scanning entire table
Like an index at the back of a book!
```

**Indexes we added:**
```
users table:
→ idx_user_email (UNIQUE) → fast login lookup

profiles table:
→ idx_profile_user_id (UNIQUE) → fast profile fetch

videos table:
→ idx_video_id (UNIQUE)      → fast duplicate check
→ idx_video_category (NORMAL) → fast feed query
→ idx_video_published_at      → fast date sorting
```

**Why Indexing:**
```
Without index:
→ SELECT * FROM videos WHERE category = 'F1'
→ Scans ALL rows in table → slow! ❌

With index:
→ Same query
→ Jumps directly to F1 rows → fast! ✅
```

**Real numbers:**
```
1 million videos without index → 2 seconds
1 million videos with index    → 2 milliseconds
1000x faster! 🚀
```

**Problems with too many indexes:**
```
❌ Slows down INSERT and UPDATE operations
❌ Takes up extra disk space
❌ Over indexing can hurt performance
```

**How to improve:**
```
→ Only index columns used in WHERE/ORDER BY
→ Use EXPLAIN to analyze slow queries
→ Monitor index usage regularly
→ Add composite indexes for multi-column queries
```

---

### 2.4 Entity Relationships

**What:**
```
How tables connect to each other
```

**Relationships in our project:**
```
users ──── profiles   (One to One)
→ One user has exactly one profile
→ profile has user_id as foreign key

videos/articles → no direct user relation
→ Shared across all users
→ Filtered by category matching user genres
```

**Why proper relationships:**
```
✅ Data integrity → can't have profile without user
✅ Easy to query related data
✅ No duplicate data
✅ Cascading operations
```

**Problems without relationships:**
```
❌ Orphan records (profiles without users)
❌ Data inconsistency
❌ Manual cleanup needed
```

**How to improve:**
```
→ Add @OneToMany between user and saved videos
→ Add user_id to articles for personalization
→ Add cascade delete for user cleanup
```

---

### 2.5 ENUM Types

**What:**
```
ENUM = predefined set of allowed values
Only specific values can be stored
```

**How we used it:**
```
VideoCategory enum:
→ F1
→ CRICKET

Used in:
→ Video.category
→ Article.category
→ Profile.genres
```

**Why ENUM:**
```
✅ Prevents invalid category values
✅ Type safe in code
✅ Clear readable code
✅ Database level constraint
```

**Problems without ENUM:**
```
❌ Anyone can store "FOOTBALL" as category
❌ Typos like "f1" or "F-1" cause bugs
❌ No validation at code level
```

**How to improve:**
```
→ Add more categories (FOOTBALL, TENNIS etc.)
→ Make categories configurable from DB
→ Add category display names
```

---

### 2.6 Auto DDL

**What:**
```
Hibernate automatically creates/updates 
database tables based on entity classes
No manual SQL scripts needed
```

**How we configured it:**
```properties
spring.jpa.hibernate.ddl-auto=update
```

**Modes available:**
```
create      → drop and recreate tables every restart
create-drop → drop tables when app stops
update      → only add new columns/tables ✅ (we use this)
validate    → just check tables match entities
none        → do nothing
```

**Why update mode:**
```
✅ No manual SQL scripts
✅ New columns added automatically
✅ Existing data preserved
✅ Perfect for development
```

**Problems with auto DDL:**
```
❌ Not recommended for production
❌ Cannot drop columns automatically
❌ Complex migrations not handled
```

**How to improve:**
```
→ Use Flyway or Liquibase for production
→ Version controlled migration scripts
→ Rollback support
→ Switch to validate in production
```

---

## Summary Table

| Subtopic | Problem Solved | Tech Used |
|----------|---------------|-----------|
| MySQL | Reliable data storage | MySQL 8.x |
| ORM | No manual SQL | Hibernate/JPA |
| Indexing | Fast queries | @Index annotation |
| Relationships | Data integrity | @OneToOne, FK |
| ENUM | Valid categories only | Kotlin enum class |
| Auto DDL | No manual scripts | ddl-auto=update |

---

## How Database Design Improved Our Project

```
Before good DB design:
→ Slow feed queries scanning all videos
→ Invalid categories stored
→ Duplicate videos stored
→ Manual SQL scripts needed

After good DB design:
→ Fast feed queries via category index ✅
→ Only F1/CRICKET categories allowed ✅
→ Unique index prevents duplicates ✅
→ Auto table creation on startup ✅
→ Data integrity via relationships ✅
```

---
