# X-Clone

<p align="center">
  <img src="src/main/resources/Client/images/x-logo-1.png" alt="X-Clone logo"/>
</p>

<p align="center">
  <b>A client–server social networking desktop application inspired by X (formerly Twitter).</b><br/>
  Built in Java with a JavaFX client, a multithreaded socket server, and a PostgreSQL-backed
  persistence layer designed around Clean Architecture principles.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white" alt="Java 25"/>
  <img src="https://img.shields.io/badge/JavaFX-26.0.1-4E9BCD?logo=java&logoColor=white" alt="JavaFX 26.0.1"/>
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/Database-PostgreSQL-336791?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/ORM-Hibernate%20%2F%20JPA-59666C?logo=hibernate&logoColor=white" alt="Hibernate/JPA"/>
  <img src="https://img.shields.io/badge/Protocol-JSON%20over%20TCP-yellow?logo=json&logoColor=white" alt="JSON over TCP"/>
  <img src="https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white" alt="JUnit 5"/>
  <img src="https://img.shields.io/badge/License-MIT-black.svg" alt="License"/>
</p>

---

## Table of Contents

1. [Overview](#1-overview)
2. [Features](#2-features)
3. [Architecture](#3-architecture)
4. [Tech Stack](#4-tech-stack)
5. [Project Structure](#5-project-structure)
6. [Getting Started](#6-getting-started)
7. [Running the Application](#7-running-the-application)
8. [Usage Guide](#8-usage-guide)
9. [Demo](#9-demo)
10. [Credits](#10-credits)
11. [Changelog](#11-changelog)
12. [Contact](#12-contact)

---

## 1. Overview

**X-Clone** is a desktop reproduction of the core X/Twitter experience, developed as the final
project for the *Advanced Programming* course (Summer 2026). Multiple JavaFX clients connect
concurrently to a central Java socket server, which persists all application state - users,
tweets, relationships, media, polls, and messages - in a PostgreSQL database through a
Hibernate/JPA persistence layer.

The project emphasizes:

- A real **client–server architecture** with a hand-rolled JSON communication protocol (no HTTP
  framework), served over raw TCP sockets.
- A **layered, use-case-driven backend** (`domain` -> `application` -> `infrastructure`), keeping
  business rules independent of persistence and transport concerns.
- A **normalized relational schema** with soft deletes, composite keys, and indexed
  relationship tables (follows, blocks, mutes, likes).
- Support for **many concurrent clients**, each with its own authenticated session and local
  SQLite cache.

## 2. Features

### Implemented

| Category | Capabilities |
|---|---|
| **Authentication** | Registration, login/logout, session refresh, BCrypt password hashing, forgot-password / OTP-based reset flow |
| **Profile** | Profile viewing/editing, avatar & cover image updates, username/email/password changes, account deactivation |
| **Tweets** | Create, edit (with edit history), soft-delete, reply, quote, retweet |
| **Timeline** | Home timeline with projection-based, paginated queries |
| **Interactions** | Like / unlike, with aggregated like counts |
| **Relationships** | Follow / unfollow, block / unblock, mute / unmute |
| **Hashtags** | Automatic extraction, hashtag following, trending hashtags, hashtag search |
| **Search** | User, tweet, hashtag, media, and conversation search |
| **Polls** | Poll creation, voting, and closing |
| **Direct Messages** | Conversations with multiple members, sending/editing/deleting messages |
| **Notifications** | Notification generation, read/unread state, bulk read |
| **Media** | Upload, download, and deletion of tweet attachments |
| **Concurrency** | Thread-per-client socket handling, isolated session contexts, connection pooling via HikariCP |

## 3. Architecture

The backend follows a **Clean Architecture** layering, kept independent of the JavaFX client:

```
                     ┌────────────────────────────┐
                     │           Client           │
                     │  JavaFX views + FXML       │
                     │  Client-side services      │
                     │  Local SQLite cache        │
                     └─────────────┬──────────────┘
                                   │  JSON over TCP socket
                     ┌─────────────▼──────────────┐
                     │           Server           │
                     │  SocketServer (threaded)   │
                     │  ClientHandler             │
                     │  RequestDispatcher         │
                     └─────────────┬──────────────┘
                                   │
        ┌──────────────────────────▼─────────────────────────────┐
        │                       logic_core                       │
        │                                                        │
        │  presentation   -> RequestType / Envelopes             │
        │  application    -> Facades, Use Cases, DTOs,           │
        │                    Validators, Mappers, Policies       │
        │  domain         -> Models, Repositories (interfaces),  │
        │                    Domain Events                       │
        │  infrastructure -> JPA Repositories, DAOs, Event Bus   │
        └──────────────────────────┬─────────────────────────────┘
                                   │  JPA / Hibernate
                     ┌─────────────▼──────────────┐
                     │         PostgreSQL         │
                     └────────────────────────────┘
```

Key patterns applied throughout the codebase:

- **Repository pattern** - domain repositories (`UserRepository`, `TweetRepository`,
  `RelationshipRepository`, …) are pure interfaces; JPA implementations live in
  `infrastructure/repository`.
- **Use-case-driven design** - every business operation is its own class
  (`CreateTweetUseCase`, `LoginUserUseCase`, `FollowUserUseCase`, `GetTimelineUseCase`, …).
- **Facade layer** - `TweetFacade`, `AuthFacade`, `RelationFacade`, etc. compose use cases for
  the transport layer.
- **Policy layer** - authorization rules are isolated from use cases
  (`InteractionPolicy`, `TimelinePolicy`, `FollowPolicy`, `BlockPolicy`, …).
- **Event-driven side effects** - domain events (`TweetCreatedEvent`, `UserFollowedEvent`,
  `MessageSentEvent`, …) are dispatched through an `EventBus` after a use case succeeds.
- **Result wrapper** - a unified `Result<T>` (`Success` / `Failure`) replaces exceptions for
  expected control flow.
- **Generic DAO + JPQL projections** - `AbstractJpaDao` and `GenericDAO<T>` centralize CRUD,
  while dedicated projection DTOs (`TimelineTweetProjection`) avoid loading full entity graphs
  for read-heavy timeline queries.
- **Soft delete** - entities implement `SoftDeletable`, supporting soft delete, tweet
  redaction, and cascading hard deletes where required.

## 4. Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 25 |
| Build tool | Maven |
| GUI | JavaFX 26 (FXML + CSS) |
| Server transport | Raw TCP sockets, thread-per-client |
| Serialization | Gson (JSON) |
| Database | PostgreSQL |
| ORM | Hibernate / Jakarta Persistence (JPA) |
| Connection pooling | HikariCP |
| Client-side cache | SQLite (via Hibernate community dialect) |
| Password hashing | jBCrypt |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5, Mockito |
| Logging | SLF4J |

## 5. Project Structure

```
X-Clone/
├── src/main/java/
│   ├── Client/                   JavaFX application, controllers, client-side services
│   ├── Server/                   Server bootstrap and server-side DAO manager
│   ├── Shared/                   JPA entities, DAOs, and database bootstrap shared by client & server
│   ├── logic_core/
│   │   ├── app/                  Use cases, facades, DTOs, validators, mappers, security
│   │   ├── domain/               Domain models, repository interfaces, policies, events
│   │   ├── infrastructure/       JPA repository implementations, DAOs, transport layer, event bus
│   │   ├── common/               Shared exceptions, Result wrapper, utilities
│   │   └── session/              Server-side session management
│   └── enums/                    Shared enumerations
├── src/main/resources/
│   ├── Client/fxml/              FXML view definitions
│   ├── Client/css/               Application stylesheet
│   ├── Client/images/            Icons and static assets
│   └── META-INF/persistence.xml  JPA persistence units (PostgreSQL + local SQLite cache)
├── src/test/java/Testing/        Unit, integration, and concurrency tests
└── pom.xml
```

## 6. Getting Started

### Prerequisites

- **JDK 25** or later
- **Maven 3.9+**
- **PostgreSQL 14+** running locally (or reachable over the network)
- A JavaFX-capable desktop environment (Windows, macOS, or Linux)

### Database setup

Create the target database before first launch:

```sql
CREATE DATABASE xclonedb;
```

Connection settings are configured in
`src/main/resources/META-INF/persistence.xml` (JDBC URL, username, and password). Schema
objects are created and kept in sync automatically via `hibernate.hbm2ddl.auto=update` on
first run.

### Clone & build

```bash
git clone <repository-url>
cd X-Clone
mvn clean compile
```

## 7. Running the Application

The project ships two entry points: the **server** and the **client**. Start the server first,
then one or more clients.

### 1. Start the server

```bash
mvn exec:java -Dexec.mainClass="logic_core.infrastructure.transport.server.ServerMain" -Dexec.args="--port=8080"
```

- The port is configurable via `--port=<PORT>`, a bare positional argument, or the
  `XCLONE_SERVER_PORT` environment variable; it defaults to `8888` if omitted.
- If the chosen port is already in use, free it first. On Windows:

  ```bash
  netstat -ano | findstr :8080
  taskkill /PID <pid> /F
  ```

  On macOS/Linux:

  ```bash
  lsof -i :8080
  kill -9 <pid>
  ```

### 2. Start one or more clients

In a separate terminal:

```bash
mvn -q compile exec:java -Dexec.mainClass=Client.MainApp
```

Repeat this command in additional terminals to simulate multiple concurrent users.

## 8. Usage Guide

1. **Register** a new account from the client's registration screen (unique username, email,
   and password).
2. **Log in** to establish a session with the server.
3. **Compose a tweet** from the timeline view; hashtags and mentions are detected automatically.
4. **Follow other users** to populate your personalized home timeline.
5. **Interact** with tweets via like, reply, retweet, or quote.
6. **Message** other users through the conversations panel.
7. **Search** for users, tweets, or hashtags using the search bar.
8. **Manage your profile** - update your bio, avatar, cover image, username, email, or
   password from the profile screen.

## 9. Demo
<img src="ERD.png" alt="ERD"/>
*More to be added*

## 10. Credits

Developed by:
- [**Alireza Heydari**](https://github.com/AlirezaHeydari-Dev)
- [**Mohammadreza Ashrafian**](https://github.com/mohammadrezaashrafian)
- [**Amir Mohammad Talaei**](https://github.com/amirmt86)

## 11. Changelog

| Version | Notes |
|---|---|
| 1.0-SNAPSHOT | Core authentication, tweets, timeline, social graph, hashtags, polls, direct messaging, and notification backend implemented |

## 12. Contact

For questions about this project, please reach out via the team's GitHub organization
repository issues page.