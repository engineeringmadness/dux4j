# Dux4j Spring Boot Starter

A **Bookstore REST API** example demonstrating [Dux4j](https://github.com/compscikaran/dux4j) — a Redux-style unidirectional state management library for Java — integrated into a Spring Boot application.

## Architecture

```
POST /api/dispatch ──► MainController ──► DuxSlice (Redux Store)
                                              │
                        ┌─────────────────────┼─────────────────────┐
                        ▼                     ▼                     ▼
                   addBook              removeBook             addRating
                   reducer              reducer               reducer
                        │                     │                     │
                        └─────────────────────┼─────────────────────┘
                                              ▼
                                       BookStoreState
                                        List<Book>
                                              │
                                        subscriber fires
                                              │
                                              ▼
                                       SQLite (books.db)
```

- **Controller** receives all mutations through a single `POST /api/dispatch` endpoint, dispatching actions by name
- **DuxSlice** holds the complete application state in memory as a `BookStoreState` bean
- **Reducers** (`addBook`, `removeBook`, `addRating`) mutate state immutably via deep clone
- **Subscriber** syncs state to SQLite after every change
- **Startup** loads persisted books from the database; **shutdown** persists final state

## Quick Start

```bash
cd spring-boot-starter
mvn spring-boot:run
```

App starts on `http://localhost:8080`.

## API

All mutations go through a single endpoint. Reads use dedicated endpoints.

### Add a Book

```bash
curl -X POST http://localhost:8080/api/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "type": "addBook",
    "payload": {
      "title": "1984",
      "summary": "A dystopian novel",
      "author": "George Orwell",
      "isbn": "978-0451524935",
      "numOfPages": 328,
      "rating": 0
    }
  }'
```

### Add a Rating (1-5)

```bash
curl -X POST http://localhost:8080/api/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "type": "addRating",
    "payload": {
      "isbn": "978-0451524935",
      "rating": 5
    }
  }'
```

### Remove a Book

```bash
curl -X POST http://localhost:8080/api/dispatch \
  -H "Content-Type: application/json" \
  -d '{
    "type": "removeBook",
    "payload": "978-0451524935"
  }'
```

### List All Books

```bash
curl http://localhost:8080/api/books
```

## Project Structure

```
src/main/java/dev/engineeringmadness/starter/
├── DuxSpringBootStarterApplication.java   # Entry point
├── controller/
│   └── MainController.java               # REST endpoints
├── domain/
│   ├── Book.java                          # State POJO
│   ├── BookEntity.java                    # JPA entity (SQLite)
│   ├── BookStoreState.java                # Redux state (implements State)
│   ├── DispatchRequest.java               # Generic dispatch DTO
│   └── AddRatingRequest.java              # Rating payload DTO
├── logic/
│   └── BookStore.java                     # DuxSlice bean config + reducers
└── repository/
    └── BookRepository.java                # Spring Data JPA repository

src/main/resources/
├── application.properties                 # SQLite datasource config
└── bookstore.db                           # SQLite database file
```

## Tech Stack

| Dependency | Purpose |
|---|---|
| Dux4j 2.0.0 | Redux-style state management |
| Spring Boot 4.1.0 | Application framework |
| Spring Data JPA | Database access layer |
| SQLite + Hibernate Community Dialects | Embedded persistence |
| Lombok | Boilerplate reduction |
| ModelMapper | Entity ↔ POJO mapping |
