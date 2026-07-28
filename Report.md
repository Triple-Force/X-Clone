# Project Report - X-Clone

**Course:** Advanced Programming - Summer 2026
**Instructor:** Dr. Saeed Reza Kheradpisheh
**Repository:** `Triple-Force/X-Clone` (Maven `groupId`: `org.tripleforce`)

---

## Table of Contents

1. [System Architecture](#1-system-architecture)
2. [Database Design](#2-database-design)
3. [Object-Oriented Design](#3-object-oriented-design)
4. [AI Usage Disclosure](#4-ai-usage-disclosure)

---

## 1. System Architecture

### 1.1 Overview

X-Clone is a **client–server desktop application**. A single, long-running Java server accepts
concurrent TCP socket connections from any number of JavaFX clients. All durable state lives in
a central PostgreSQL database; each client additionally keeps a small local SQLite cache for
offline/read-optimized access to previously fetched data.

There is no HTTP layer - client and server exchange **JSON-encoded request/response envelopes**
over raw sockets, serialized with Gson.

### 1.2 Client and Server Components

**Client (`Client` package)**
- `MainApp` - JavaFX application entry point and stage bootstrap.
- `NavigationManager` - controls scene/view transitions.
- `controllers/*` - one FXML controller per screen (Login, Register, Timeline, Profile,
  Messages, Tweet item, password reset flow).
- `Service/*` - thin client-side service classes (`AuthClientService`, `TweetClientService`,
  `TimelineClientService`, `RelationClientService`, `MessageClientService`,
  `ConversationClientService`) that build request DTOs and talk to the server through
  `transport.SocketClient`.
- `session.ClientSession` - holds the authenticated session/token for the running client.
- `ClientCacheDatabase` / `ClientDAOManager` - local SQLite-backed cache, sharing the same
  Hibernate entity model as the server's persistence unit.

**Server (`Server` package + `logic_core.infrastructure.transport.server`)**
- `ServerMain` - process entry point; resolves the listening port from CLI args or the
  `XCLONE_SERVER_PORT` environment variable (defaulting to `8888`).
- `SocketServer` - opens the listening socket and hands off each accepted connection.
- `ClientHandler` - runs on a dedicated thread per connected client, reading request envelopes
  and writing response envelopes.
- `RequestDispatcher` - routes each `RequestType` to the appropriate application-layer facade.
- `ServerDAOManager` - manages the server-side JPA `EntityManagerFactory` and connection pool.

**Shared (`Shared` package)**
- JPA entity model (`Shared.Models.*`), the generic DAO (`GenericDAO<T>`), and
  `DatabaseInitializer` classes (`DatabaseCreator`, `DatabaseSeeder`) used to provision and seed
  the schema. This package is compiled into both the client (for its local cache) and the
  server (for the primary database), which is why the two `persistence.xml` units
  (`X-Clone-PU` for PostgreSQL, `X-Clone-Client-Cache-PU` for SQLite) share the same entity
  list.

### 1.3 Communication Flow

1. The client serializes a request DTO (e.g. `CreateTweetRequest`) into a `RequestEnvelope`
   carrying a `RequestType` and a JSON payload, and writes it to its socket.
2. `ClientHandler` on the server reads the envelope and hands it to `RequestDispatcher`.
3. The dispatcher resolves the matching **facade** (e.g. `TweetFacade`), which invokes one or
   more **use cases** (e.g. `CreateTweetUseCase`).
4. The use case validates the request (`TweetValidator`), checks authorization (`InteractionPolicy`),
   performs the operation through a domain **repository**, and publishes a **domain event**
   (`TweetCreatedEvent`) via the `EventBus`.
5. The result is mapped to a response DTO (`CreateTweetResponse`) and wrapped in a
   `ResponseEnvelope`, which is written back to the requesting client's socket.

### 1.4 Architectural Patterns

The `logic_core` module is organized as a **layered / Clean Architecture** system:

| Layer | Package | Responsibility |
|---|---|---|
| Domain | `logic_core.domain` | Entity-agnostic models, repository interfaces, policies, domain events |
| Application | `logic_core.app` | Use cases, facades, DTOs, validators, mappers, security context |
| Infrastructure | `logic_core.infrastructure` | JPA repository implementations, DAOs, the socket transport layer, the event bus |
| Common | `logic_core.common` | Cross-cutting exceptions, the `Result<T>` wrapper, and utilities |

This keeps business logic (use cases, policies) free of any dependency on Hibernate, sockets, or
JSON - those concerns are confined to the infrastructure layer and injected through interfaces,
wired together in `DependencyContainer`.

### 1.5 Concurrency Model

- Each client connection is handled on its own thread (`ClientHandler`), allowing many users to
  be served simultaneously.
- `SessionManager` / `SessionFactory` and `ThreadLocalUserSessionContext` isolate the
  authenticated user's identity per request thread, preventing cross-talk between concurrently
  handled clients.
- Database access is pooled through **HikariCP**, avoiding one physical connection per client
  thread.
- Concurrency correctness is exercised directly by tests such as
  `RobustConcurrentAuthIntegrationTest`, `BrutalAuthConcurrencyTest`,
  `ConcurrentClientSocketIntegrationTest`, and `SessionContextIsolationTest`.

---

## 2. Database Design

### 2.1 Overview

Persistence is implemented with **Hibernate/JPA** against **PostgreSQL**, configured through
`src/main/resources/META-INF/persistence.xml`. UUIDs (`GenerationType.UUID`) are used as primary
keys for all entities, generated through a shared `BaseEntity` superclass that also provides a
`createdAt` timestamp via `@CreationTimestamp`.

### 2.2 Core Entities

| Entity | Purpose |
|---|---|
| `User` | Account, profile, and credential data |
| `Tweet` | Tweet content; also models replies, quotes, and retweets |
| `TweetEdit` | Historical snapshot of a tweet prior to an edit |
| `TweetHashtag`, `Hashtag` | Many-to-many association between tweets and hashtags |
| `TweetMention` | Mentions of users within a tweet |
| `Follow`, `Block`, `Mute` | Directed user-to-user relationship tables |
| `Like` | User-to-tweet interaction |
| `Media` | Attached media metadata for a tweet |
| `Poll`, `PollOption`, `PollVote` | Poll definitions, their options, and cast votes |
| `Conversation`, `ConversationMember`, `DirectMessage` | Direct-messaging domain |
| `Notification` | Per-user notification records |
| `Session` | Persisted authentication sessions |
| `HashtagFollow` | User subscriptions to hashtags |

### 2.3 Relationships and Keys

Association entities that represent a relationship between two other entities
(`Follow`, `Block`, `Mute`, `Like`, `TweetHashtag`, `TweetMention`, `HashtagFollow`,
`ConversationMember`, `PollVote`) use **composite primary keys** (e.g. `FollowId`, `BlockId`,
`LikeId`) rather than a synthetic UUID, since their identity is naturally defined by the pair of
entities they connect. This keeps the relationship tables narrow and lets the database enforce
uniqueness directly on the composite key.

Foreign keys used in timeline and relationship lookups are indexed to keep feed-generation and
relationship-check queries efficient at scale.

### 2.4 Soft Delete and Redaction

Entities that must not be physically removed immediately (most notably `Tweet`) implement the
`SoftDeletable` interface, which defines:

- `isDeleted()` / `setDeleted(boolean)` - the soft-delete flag.
- `redact()` - an optional hook to scrub sensitive content while retaining the row (tweet
  redaction after deletion).
- `onSoftDelete(EntityManager em)` - a hook for cascading side effects at the moment of soft
  deletion.
- `hardDeleteWhere(...)` - a helper for cascading a genuine hard delete of dependent rows
  where retention is not required.

### 2.5 Persistence Configuration

Two persistence units are declared against the same entity list:

- **`X-Clone-PU`** - the server's primary unit, pointed at PostgreSQL, using HikariCP pooling
  (min idle 5 / max pool 20), JDBC batching (`hibernate.jdbc.batch_size=20`), and
  `hibernate.hbm2ddl.auto=update` for schema evolution during development.
- **`X-Clone-Client-Cache-PU`** - a lightweight SQLite unit used by the client for local
  caching, with foreign keys disabled and WAL mode enabled for simpler single-user access.

### 2.6 Query Strategy

Read-heavy paths - most notably timeline generation - avoid loading full entity graphs. Instead,
JPQL **constructor projections** (e.g. `TimelineTweetProjection`) select only the columns needed
to render a timeline row, combined with aggregate subqueries for like/reply/retweet counts and
`EXISTS` checks for block/mute filtering. This is handled through dedicated DAO classes
(`AbstractJpaDao` and per-entity DAOs such as `TweetDao`, `FollowDao`, `LikeDao`) rather than by
traversing lazy-loaded entity associations.

---

## 3. Object-Oriented Design

### 3.1 Key Classes and Responsibilities

- **`BaseEntity`** - abstract superclass providing the UUID identifier, creation timestamp, and
  identity-based `equals`/`hashCode` for all JPA entities.
- **`SoftDeletable`** - interface contract for entities supporting soft delete and redaction.
- **`GenericDAO<T>`** - a generic, reusable persistence helper used by `Shared.Database` for
  simple entity CRUD, reducing duplication across entity-specific access code.
- **`AbstractJpaDao`** - the `logic_core.infrastructure` counterpart used by the application's
  DAOs, standardizing JPQL execution for the query-heavy, use-case-facing layer.
- **`Result<T>` / `Success` / `Failure`** - a discriminated result type used as the return value
  of use cases instead of throwing exceptions for expected failure paths (validation errors,
  not-found, conflicts).
- **Facades** (`AuthFacade`, `TweetFacade`, `TimelineFacade`, `RelationFacade`,
  `ConversationFacade`, `MessageFacade`) - the entry points the transport layer calls into;
  each composes one or more use cases and returns a response DTO.
- **Use cases** (e.g. `RegisterUserUseCase`, `LoginUserUseCase`, `CreateTweetUseCase`,
  `FollowUserUseCase`, `GetTimelineUseCase`, `VotePollUseCase`) - each encapsulates exactly one
  business operation, its validation, and its side effects.

### 3.2 Inheritance, Composition, and Abstraction

- **Inheritance** is used sparingly and deliberately: `BaseEntity` is the sole entity
  superclass; `MutableEntity` / `ImmutableEntity` further distinguish entities whose state can
  change after creation from those that cannot.
- **Composition** dominates the application layer - facades are composed of use cases, use
  cases are composed of repositories/validators/policies, rather than relying on deep
  inheritance chains.
- **Abstraction** is enforced through interfaces at every architectural seam: domain
  repositories (`UserRepository`, `TweetRepository`, …) are interfaces implemented by JPA
  classes in `infrastructure.repository`; `SystemMessageDispatcher`,
  `PasswordResetDeliveryPort`, and `EventPublisher` similarly decouple the application layer
  from a specific delivery/transport mechanism.
- **Polymorphism** appears through the domain event hierarchy (`DomainEvent` and its many
  subclasses per aggregate - `TweetEvent`, `AuthenticationEvent`, `RelationshipEvent`,
  `ConversationEvent`, `MessageEvent`, `PollEvent`) and through the custom exception hierarchy
  (`AppException` as the root of `ValidationException`, `NotFoundException`,
  `ConflictException`, `ForbiddenException`, `DatabaseException`, etc.), letting callers catch
  broadly or narrowly as needed.

### 3.3 Design Patterns Applied

| Pattern | Where |
|---|---|
| Repository | `domain.repository.*` interfaces + `infrastructure.repository.Jpa*Repository` implementations |
| Facade | `logic_core.app.facade.*` |
| Use Case / Command | `logic_core.app.usecase.*` |
| DTO / Mapper | `logic_core.app.dto.*`, `logic_core.app.mapper.*` |
| Observer / Publish-Subscribe | `domain.event.EventBus`, `EventPublisher`, `EventListener`, `AsyncEventBus` |
| Strategy (delivery ports) | `PasswordResetDeliveryPort` with `LoggingPasswordResetDeliveryAdapter` |
| Generic / Template DAO | `GenericDAO<T>`, `AbstractJpaDao` |
| Result / Either | `Result<T>` (`Success` / `Failure`) |
| Dependency Injection (manual) | `DependencyContainer` wiring facades, use cases, and repositories |

---

## 4. AI Usage Disclosure

*To be written*